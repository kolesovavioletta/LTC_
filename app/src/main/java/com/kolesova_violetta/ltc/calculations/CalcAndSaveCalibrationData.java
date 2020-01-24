package com.kolesova_violetta.ltc.calculations;

import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.android.volley.VolleyError;
import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.FailCallback;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.datastore.SuccessCb;
import com.kolesova_violetta.ltc.handlers.LiveDataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kolesova_violetta.ltc.handlers.LiveDataUtils.castErrorLiveData;
import static com.kolesova_violetta.ltc.handlers.LiveDataUtils.getAnswer;
import static com.kolesova_violetta.ltc.handlers.LiveDataUtils.getError;
import static com.kolesova_violetta.ltc.mock.Const.CIRCUITS_COUNT;

/**
 * Сохранить коэффициенты калибровки устройства на устройство. Т.к. напряжение у устройства
 * переменное, может понадобиться несколько попыток записи. Алгоритм:
 * 1. Посчитать коэффициенты тягача/прицепа
 * 2. Сохранить коэффициенты тягача/прицепа на датчик
 * 3. Дождаться сохранения коэффициентов и тягача, и прицепа (если необходимо)
 * 4. Получить с датчика новые массы
 * 5. Проверить корректность масс. Если массы неверные - повторить с п1, если ошибка при расчетах,
 * иначе - с п2.
 * TODO: Rx
 */
public class CalcAndSaveCalibrationData {
    private static final int DELTA_AXLE_WEIGHT = 50; //прогрешность массы оси на весах и с устройства
    private static final int COUNT_TRY_SAVE = 20; // максимальное количество попыток сохранить коэффициенты

    private enum Saved_State { // Состояния выполнения алгоритма
        NONE, // не запущен (и не будет запущен)
        WAIT, // ожидает запуска
        PROGRESS,  // сохраняется
        SUCCESS, // успешно сохранен
        FAIL // ошибка при сохранении
    }

    private Repository mDeviceRepo;
    private SharedPreferencesRepository mLocalRepo;

    // Запуск (повтор) работы класса с 1. Посчитать коэффициенты тягача/прицепа: mStartMLD.post...
    private MutableLiveData<Void> mStartMLD = new MutableLiveData<>();

    private Saved_State mHeadSavedState;
    // Переменные для запуска (повтор) работы класса с пункта 2. Сохранить коэффициенты тягача/прицепа на датчик
    //  - основного датчика
    private Response<float[], Exception> mCoefHead; // Ответ на сохранение коэффициентов
    // Сюда устанавливается ответ mCoefHead
    private MutableLiveData<Response<float[], Exception>> mCoefHeadMLD = new MutableLiveData<>();
    // И запускаются следующие шаги с помощью  MediatorLiveData mWaitForSaveAndCheckHead
    private MediatorLiveData<Response<float[], Exception>> mWaitForSaveAndCheckHead = new MediatorLiveData<>();
    // - дочернего датчика
    private Saved_State mTrailerSavedState;
    private MutableLiveData<Response<float[], Exception>> mCoefTrailerMLD = new MutableLiveData<>();
    private Response<float[], Exception> mCoefTrailer;
    private MediatorLiveData<Response<float[], Exception>> mWaitForSaveAndCheckTrailer;
    // Переменные для объединения дальнейших действий после расчета и сохранения коэф. при 2 датчиках
    private MediatorLiveData<Response<Void, Exception>> mWaitFinishSaveTwoDevice = new MediatorLiveData<>();
    private LiveData<Response<Void, Exception>> mSaveTractor;
    private LiveData<Response<Void, Exception>> mSaveTrailer;

    // Список номеров контуров для которых коэф. неверные по физическим причинам
    private boolean mErrorDeviceHead = false;
    private boolean mErrorDeviceTailer = false;

    private List<Circuit> mCircuitsHead; // контура, которые должны быть сохранены
    private List<Circuit> mCircuitsTrailer;

    private String mDriverName;
    private int steeringAxleWeight;

    public CalcAndSaveCalibrationData(Repository mDeviceRepo, SharedPreferencesRepository mLocalRepo) {
        this.mDeviceRepo = mDeviceRepo;
        this.mLocalRepo = mLocalRepo;
    }

    public LiveData<Response<boolean[], Throwable>> start() {
        mHeadSavedState = Saved_State.WAIT;
        mTrailerSavedState = Saved_State.NONE;

        // Подготовка данных для запроса-сохранения данных калибровки
        mDriverName = mLocalRepo.getDriverName();
        steeringAxleWeight = mLocalRepo.getSteeringAxleWeight();
        // Запуск алгоритма
        mStartMLD.setValue(null);

        int modeInstallation = mLocalRepo.getModeInstallation();
        mCircuitsHead = getCircuitsHead(modeInstallation);
        // Сохранение данных на один или два датчика
        boolean trailerNeedSave = modeInstallation == 2 && mLocalRepo.isExistTrailer();
        if(trailerNeedSave) {
            mTrailerSavedState = Saved_State.WAIT;
        }
        LiveData<Response<Void, Exception>> saved = Transformations.switchMap(mStartMLD,
                x -> trailerNeedSave ?
                        startSaveForTwoDevice() :
                        startSaveHead());

        // Получуение масс, основанных на новых коэффициентах
        LiveData<Response<JSONObject, Exception>> weightsResponse = getWeightsFromDevice(saved);
        // Проверка соответствия полученных с датчика масс с введенными пользователем
        return check(weightsResponse);
    }

    private List<Circuit> getCircuitsHead(int modeInstallation) {
        List<Circuit> circuits = new ArrayList<>(CIRCUITS_COUNT);
        switch (modeInstallation) {
            case 1:
                circuits = mLocalRepo.getCircuitsTrailer();
                break;
            case 2:
                circuits = mLocalRepo.getCircuitsTractor();
                break;
            case 3:
            case 4:
                circuits = mLocalRepo.getCircuitsTrailer();
                circuits.addAll(mLocalRepo.getCircuitsTractor());
                break;
        }
        return circuits;
    }

    private LiveData<Response<Void, Exception>> startSaveHead() {
        // Расчет коэффициентов для калибровки датчика
        return calcAndSaveHead(mCircuitsHead);
    }

    private LiveData<Response<Void, Exception>> startSaveForTwoDevice() {
        // Расчет коэффициентов для калибровки датчика тягача
        mSaveTractor = calcAndSaveHead(mCircuitsHead);

        // Расчет коэффициентов для калибровки датчика прицепа
        mCircuitsTrailer = mLocalRepo.getCircuitsTrailer();
        mSaveTrailer = calcAndSaveTrailer(mCircuitsTrailer);

        // Подождать завершения сохранения для тягача и прицепа
        mWaitFinishSaveTwoDevice = waitFinishSave(mSaveTractor, mSaveTrailer);
        return mWaitFinishSaveTwoDevice;
    }

    /**
     * Расчет коэффициентов для основного датчика и сохранение их на него
     */
    private LiveData<Response<Void, Exception>> calcAndSaveHead(List<Circuit> circuits) {
        // Расчет коэффициентов
        CalcHeadCalibrCoefExecutor calcExec = new CalcHeadCalibrCoefExecutor(mDeviceRepo);
        LiveData<Response<float[], Exception>> calcResponse = calcExec.runCalc(circuits);
        // Сохранение ответа с массивом коэффициентов в mCoefHead
        LiveData<Response<float[], Exception>> calcArr = Transformations.map(calcResponse, input -> {
            mCoefHead = input;
            if (input.isSuccess()) {
                float[] coef = getAnswer(input);
                mErrorDeviceHead = coefArrWithError(coef);
            }
            return input;
        });
        // Объединение двух веток данных: данные при первой попытке, данные следующих попыток
        mWaitForSaveAndCheckHead = combineTwoLiveData(calcArr, mCoefHeadMLD);
        return saveTractorDataOnDevice(mWaitForSaveAndCheckHead);
    }

    /**
     * Расчет коэффициентов для дочернего датчика и сохранение их на него
     */
    private LiveData<Response<Void, Exception>> calcAndSaveTrailer(List<Circuit> circuits) {
        // Расчет коэффициентов
        CalcTrailerCalibrCoefExecutor calcExec = new CalcTrailerCalibrCoefExecutor(mDeviceRepo);
        LiveData<Response<float[], Exception>> calcResponse = calcExec.runCalc(circuits);
        // Сохранение ответа с массивом коэффициентов в mCoefTrailer
        LiveData<Response<float[], Exception>> calcArr = Transformations.map(calcResponse, input -> {
            mCoefTrailer = input;
            if (input.isSuccess()) {
                float[] coef = getAnswer(input);
                mErrorDeviceTailer = coefArrWithError(coef);
            }
            return input;
        });
        // Объединение двух веток данных: данные при первой попытки, данные следующих попыток
        mWaitForSaveAndCheckTrailer = combineTwoLiveData(calcArr, mCoefTrailerMLD);
        return saveTrailerDataOnDevice(mWaitForSaveAndCheckTrailer);
    }

    private boolean coefArrWithError(float[] coef) {
        for (float v : coef) {
            if (v < 0) {
                return true;
            }
        }
        return false;
    }

    private LiveData<Response<Void, Exception>> saveTractorDataOnDevice(
            LiveData<Response<float[], Exception>> coefLiveData) {
        return Transformations.switchMap(coefLiveData, coefResp -> {
            mHeadSavedState = Saved_State.PROGRESS;
            if (coefResp.isSuccess()) {
                float[] coef = getAnswer(coefResp);
                LiveData<Response<Void, VolleyError>> save = mDeviceRepo.setTractorCalibration_OnDevice(coef, steeringAxleWeight, mDriverName);
                return Transformations.map(save, LiveDataUtils::upCastLiveData);
            } else {
                return castErrorLiveData(coefResp);
            }
        });
    }

    private LiveData<Response<Void, Exception>> saveTrailerDataOnDevice(
            LiveData<Response<float[], Exception>> coefLiveData) {
        return Transformations.switchMap(coefLiveData, coefResp -> {
            mTrailerSavedState = Saved_State.PROGRESS;
            if (coefResp.isSuccess()) {
                float[] coef = getAnswer(coefResp);
                LiveData<Response<Void, VolleyError>> save = mDeviceRepo.setTrailerCalibration_OnDevice(coef, mDriverName);
                return Transformations.map(save, LiveDataUtils::upCastLiveData);
            } else {
                return castErrorLiveData(coefResp);
            }
        });
    }

    private boolean allRequestsFinishSuccess() {
        boolean headSuccess = mHeadSavedState.equals(Saved_State.SUCCESS);
        boolean trailerSuccessOrNotRun =
                mTrailerSavedState.equals(Saved_State.SUCCESS) ||
                        mTrailerSavedState.equals(Saved_State.NONE);
        return (headSuccess && trailerSuccessOrNotRun);
    }

    private LiveData<Response<JSONObject, Exception>> getWeightsFromDevice(
            LiveData<Response<Void, Exception>> onSaved) {
        return Transformations.switchMap(onSaved, input -> {
            if (input.isSuccess()) {
                LiveData<Response<JSONObject, VolleyError>> weights = mDeviceRepo.getWeights_FromDevice();
                return Transformations.map(weights, LiveDataUtils::upCastLiveData);
            } else {
                return castErrorLiveData(input);
            }
        });
    }

    /**
     * Объединение двух источников данных.
     * @param liveData1 отпишется после первой передачи значения
     * @param liveData2 необходимо отписать!
     */
    private <T> MediatorLiveData<Response<T, Exception>> combineTwoLiveData(
            LiveData<Response<T, Exception>> liveData1, LiveData<Response<T, Exception>> liveData2) {
        MediatorLiveData<Response<T, Exception>> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.addSource(liveData1, s -> {
            liveDataMerger.setValue(s);
            liveDataMerger.removeSource(liveData1);
        });
        liveDataMerger.addSource(liveData2, s -> {
            if(BuildConfig.DEBUG) {
                Log.d("!@#$", "Te:combineTwoLiveData:276: ");
            }
            liveDataMerger.setValue(s);
        });

        return liveDataMerger;
    }

    /**
     * Ожидание завершения запросов сохранения коэффициентов.
     * Передать знаечние, если оба запроса завершны или текущий завершился с ошибкой.
     * Ошибка второго запроса НЕ будет обработана.
     */
    private MediatorLiveData<Response<Void, Exception>> waitFinishSave(
            LiveData<Response<Void, Exception>> saveTractor,
            LiveData<Response<Void, Exception>> saveTrailer) {
        MediatorLiveData<Response<Void, Exception>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(saveTractor, response -> {
            mHeadSavedState = response.isSuccess() ? Saved_State.SUCCESS : Saved_State.FAIL;
            boolean curFail = mHeadSavedState.equals(Saved_State.FAIL);
            if (allRequestsFinishSuccess() || curFail) {
                mediatorLiveData.setValue(response);
            }
        });
        mediatorLiveData.addSource(saveTrailer, response -> {
            mTrailerSavedState = response.isSuccess() ? Saved_State.SUCCESS : Saved_State.FAIL;
            boolean curFail = mTrailerSavedState.equals(Saved_State.FAIL);
            if (allRequestsFinishSuccess() || curFail) {
                mediatorLiveData.setValue(response);
            }
        });
        return mediatorLiveData;
    }

    /**
     * Проверка соответствия масс. Если массы +-{@param DELTA_AXLE_WEIGHT} для каждой оси контура -
     * передается положительный ответ. В случае ошибки повторяется цикл:
     *  - посчитать-сохранить-прочитать_массы-проверить при ошибке в этапе "посчитать"
     *  - сохранить-прочитать_массы-проверить при ошибке после этапа "посчитать".
     *  Если за {@param COUNT_TRY_SAVE} циклов не удалось успешно сохранить коэффициенты -
     *  передать ошибку и закончить работу.
     * @param weightsResponse массы, полученые с датчика после смены коэффициентов.
     * @return успех/неудача/неудача попытки N < {@param COUNT_TRY_SAVE}
     */
    private LiveData<Response<boolean[], Throwable>> check(
            LiveData<Response<JSONObject, Exception>> weightsResponse) {
        return Transformations.map(weightsResponse,
                new Function<Response<JSONObject, Exception>, Response<boolean[], Throwable>>() {
            private int mNumberTrySave = 1; // количество попыток сохранить коэффициенты
            @Override
            public Response<boolean[], Throwable> apply(Response<JSONObject, Exception> input) {
                if (input.isSuccess()) {
                    List<Circuit> unoin = new ArrayList<>(mCircuitsHead);
                    unoin.addAll(mCircuitsTrailer);
                    boolean correctSaved = checkSavedWeights(getAnswer(input), unoin);
                    if (correctSaved) {
                        onDestroy();
                        return new SuccessCb<>(new boolean[]{mErrorDeviceHead, mErrorDeviceTailer});
                    } else if (mNumberTrySave < COUNT_TRY_SAVE) {
                        if(BuildConfig.DEBUG) {
                            Log.d("!@#$", "Te:check:365: TRY " + mNumberTrySave);
                        }
                        if (mCoefHead.isSuccess() && mCoefTrailer.isSuccess()) {
                            mCoefHeadMLD.setValue(mCoefHead);
                            mCoefTrailerMLD.setValue(mCoefTrailer);

                        } else {
                            mStartMLD.setValue(null);
                        }
                        mNumberTrySave++;
                        return new FailCallback<>();
                    } else {
                        onDestroy();
                        return new FailCallback<>(
                                new IndexOutOfBoundsException("the limit of attempts to write data to the device has been exhausted"));
                    }
                } else {
                    return new FailCallback<>(getError(input));
                }
            }
        });
    }

    private boolean checkSavedWeights(JSONObject deviceWeightsJson, List<Circuit> circuits) {

        if (BuildConfig.DEBUG) {
            Log.d("!@#$", "CalibrationViewModel:checkSavedWeights:113: " +
                    "Массы с устройства: " + deviceWeightsJson +
                    "\nМассы, введенные пользователем: " + circuits);
        }

        int trueWeight;
        int deviceWeight;
        int axesCount;
        for (Circuit circuit : circuits) {
            try {
                deviceWeight = deviceWeightsJson.getInt(circuit.getVar1());
            } catch (JSONException e) {
                e.printStackTrace();
                deviceWeight = 0;
            }

            axesCount = circuit.getAxesCount();
            if (axesCount < 1) axesCount = 1;
            int allowableDeltaForCirc = DELTA_AXLE_WEIGHT * axesCount;

            trueWeight = circuit.getWeight();

            if (Math.abs(deviceWeight - trueWeight) > allowableDeltaForCirc) return false;
        }

        return true;
    }

    private void onDestroy() {
        mWaitForSaveAndCheckHead.removeSource(mCoefHeadMLD);
        mWaitForSaveAndCheckTrailer.removeSource(mCoefTrailerMLD);
        mWaitFinishSaveTwoDevice.removeSource(mSaveTractor);
        mWaitFinishSaveTwoDevice.removeSource(mSaveTrailer);
    }
}
