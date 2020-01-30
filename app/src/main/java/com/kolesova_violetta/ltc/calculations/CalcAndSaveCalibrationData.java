package com.kolesova_violetta.ltc.calculations;

import android.util.Log;

import androidx.lifecycle.Observer;

import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.CustomData;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private Repository mDeviceRepo;
    private SharedPreferencesRepository mLocalRepo;
    // Запуск (повтор) работы класса с 1. Посчитать коэффициенты тягача/прицепа: mStartMLD.post...
    private CustomData<Void> mStartMLD = new CustomData<>();
    private Saved_State mHeadSavedState;
    // Переменные для запуска (повтор) работы класса с пункта 2. Сохранить коэффициенты тягача/прицепа на датчик

    //  - основного датчика
    private Response<float[], Exception> mCoefHead; // Ответ на сохранение коэффициентов
    // Сюда устанавливается ответ mCoefHead
    private CustomData<float[]> mCoefHeadMLD = new CustomData<>();
    // И запускаются следующие шаги с помощью mWaitForSaveAndCheckHead
    private CustomData<float[]> mWaitForSaveAndCheckHead;

    // - дочернего датчика
    private Saved_State mTrailerSavedState;
    private CustomData<float[]> mCoefTrailerMLD = new CustomData<>();
    private Response<float[], Exception> mCoefTrailer;
    private CustomData<float[]> mWaitForSaveAndCheckTrailer;

    // Переменные для объединения дальнейших действий после расчета и сохранения коэф. при 2 датчиках
    private CustomData<Void> mWaitFinishSaveTwoDevice;
    private CustomData<Void> mSaveTractor;
    private CustomData<Void> mSaveTrailer;
    // Список номеров контуров для которых коэф. неверные по физическим причинам
    private boolean mErrorDeviceHead = false;
    private boolean mErrorDeviceTailer = false;
    private List<Circuit> mCircuitsHead; // контура, которые должны быть сохранены
    private List<Circuit> mCircuitsTrailer;
    private String mDriverName;
    private int steeringAxleWeight;

    private CustomData.SwitchMapFun<float[], Void> saveTractorDataOnDevice = coef -> {
        mHeadSavedState = Saved_State.PROGRESS;
        if (coef.isSuccess()) {
            return mDeviceRepo.setTractorCalibration_OnDevice(coef.getResult(), steeringAxleWeight, mDriverName);
        } else {
            CustomData<Void> cd = new CustomData<>();
            cd.setValue(Response.error(coef.getError()));
            return cd;
        }
    };

    private CustomData.SwitchMapFun<float[], Void> saveTrailerDataOnDevice = coef -> {
        mTrailerSavedState = Saved_State.PROGRESS;
        if (coef.isSuccess()) {
            return mDeviceRepo.setTrailerCalibration_OnDevice(coef.getResult(), mDriverName);
        } else {
            CustomData<Void> cd = new CustomData<>();
            cd.setValue(Response.error(coef.getError()));
            return cd;
        }
    };

    private CustomData.SwitchMapFun<Void, JSONObject> getWeightsFromDevice =
            val -> mDeviceRepo.getWeights_FromDevice();
    /**
     * Проверка соответствия масс. Если массы +-{@param DELTA_AXLE_WEIGHT} для каждой оси контура -
     * передается положительный ответ. В случае ошибки повторяется цикл:
     * - посчитать-сохранить-прочитать_массы-проверить при ошибке в этапе "посчитать"
     * - сохранить-прочитать_массы-проверить при ошибке после этапа "посчитать".
     * Если за {@param COUNT_TRY_SAVE} циклов не удалось успешно сохранить коэффициенты -
     * передать ошибку и закончить работу.
     *
     * @param weightsResponse массы, полученые с датчика после смены коэффициентов.
     * @return успех/неудача/неудача попытки N < {@param COUNT_TRY_SAVE}
     */
    private CustomData.MapFun<JSONObject, boolean[]> check =
            new CustomData.MapFun<JSONObject, boolean[]>() {

                private int mNumberTrySave = 1; // количество попыток сохранить коэффициенты

                @Override
                public Response<boolean[], Exception> apply(Response<JSONObject, Exception> val) {
                    List<Circuit> unoin = new ArrayList<>(mCircuitsHead);
                    unoin.addAll(mCircuitsTrailer);
                    boolean correctSaved = checkSavedWeights(val.getResult(), unoin);
                    if (correctSaved) {
                        onDestroy();
                        return Response.success(new boolean[]{mErrorDeviceHead, mErrorDeviceTailer});
                    } else if (mNumberTrySave < COUNT_TRY_SAVE) {
                        if (mCoefHead.isSuccess() && mCoefTrailer.isSuccess()) {
                            mCoefHeadMLD.setValue(mCoefHead);
                            mCoefTrailerMLD.setValue(mCoefTrailer);

                        } else {
                            mStartMLD.setValue(null);
                        }
                        mNumberTrySave++;
                        return Response.error(null);
                    } else {
                        onDestroy();
                        return Response.error(
                                new IndexOutOfBoundsException("the limit of attempts to write data to the device has been exhausted"));
                    }
                }
            };

    public CalcAndSaveCalibrationData(Repository mDeviceRepo, SharedPreferencesRepository mLocalRepo) {
        this.mDeviceRepo = mDeviceRepo;
        this.mLocalRepo = mLocalRepo;
    }

    public CustomData<boolean[]> start() {
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
        if (trailerNeedSave) {
            mTrailerSavedState = Saved_State.WAIT;
        }
        return mStartMLD
                .switchMap(input -> trailerNeedSave ? startSaveForTwoDevice() : startSaveHead())
                .switchMap(getWeightsFromDevice) // Получуение масс, основанных на новых коэффициентах
                .map(check); // Проверка соответствия полученных с датчика масс с введенными пользователем
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

    private CustomData<Void> startSaveHead() {
        // Расчет коэффициентов для калибровки датчика
        return calcAndSaveHead(mCircuitsHead);
    }

    private CustomData<Void> startSaveForTwoDevice() {
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
    private CustomData<Void> calcAndSaveHead(List<Circuit> circuits) {
        // Расчет коэффициентов
        CalcHeadCalibrCoefExecutor calcExec = new CalcHeadCalibrCoefExecutor(mDeviceRepo);
        CustomData<float[]> calcArr = calcExec.runCalc(circuits)
                .map(coef -> {
                    mCoefHead = coef;
                    if (coef.isSuccess()) {
                        mErrorDeviceHead = coefArrWithError(coef.getResult());
                    }
                    return mCoefHead;
                });
        // Объединение двух веток данных: данные при первой попытке, данные следующих попыток
        mWaitForSaveAndCheckHead = combineAfterCalc(calcArr, mCoefHeadMLD);
        return mWaitForSaveAndCheckHead
                .switchMap(saveTractorDataOnDevice);
    }

    /**
     * Расчет коэффициентов для дочернего датчика и сохранение их на него
     */
    private CustomData<Void> calcAndSaveTrailer(List<Circuit> circuits) {
        // Расчет коэффициентов
        CalcTrailerCalibrCoefExecutor calcExec = new CalcTrailerCalibrCoefExecutor(mDeviceRepo);
        CustomData<float[]> calcArr = calcExec.runCalc(circuits)
                .map(coef -> {
                    if (coef.isSuccess()) {
                        mErrorDeviceTailer = coefArrWithError(coef.getResult());
                    }
                    mCoefTrailer = coef;
                    return mCoefTrailer;
                });
        // Объединение двух веток данных: данные при первой попытки, данные следующих попыток
        mWaitForSaveAndCheckTrailer = combineAfterCalc(calcArr, mCoefTrailerMLD);
        return mWaitForSaveAndCheckTrailer
                .switchMap(saveTrailerDataOnDevice);
    }

    private boolean coefArrWithError(float[] coef) {
        for (float v : coef) {
            if (v < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean allRequestsFinishSuccess() {
        boolean headSuccess = mHeadSavedState.equals(Saved_State.SUCCESS);
        boolean trailerSuccessOrNotRun =
                mTrailerSavedState.equals(Saved_State.SUCCESS) ||
                        mTrailerSavedState.equals(Saved_State.NONE);
        return (headSuccess && trailerSuccessOrNotRun);
    }

    /**
     * Объединение двух источников данных.
     *
     * @param liveData1 отпишется после первой передачи значения
     * @param liveData2 необходимо отписать!
     */
    private <T> CustomData<T> combineAfterCalc(CustomData<T> liveData1, CustomData<T> liveData2) {
        CustomData<T> liveDataMerger = new CustomData<>();
        Observer<Response<T, Exception>> obs1 = liveDataMerger::setValue;
        Observer<Response<T, Exception>> obs2 = value -> {
            liveDataMerger.setValue(value);
            liveDataMerger.removeSource(liveData1);
        };
        return liveData1.combine(liveData2, obs1, obs2, liveDataMerger);
    }

    /**
     * Ожидание завершения запросов сохранения коэффициентов.
     * Передать знаечние, если оба запроса завершны или текущий завершился с ошибкой.
     * Ошибка второго запроса НЕ будет обработана.
     */
    private CustomData<Void> waitFinishSave(CustomData<Void> saveTractor, CustomData<Void> saveTrailer) {
        CustomData<Void> mediatorLiveData = new CustomData<>();
        Observer<Response<Void, Exception>> trac = response -> {
            mHeadSavedState = response.isSuccess() ? Saved_State.SUCCESS : Saved_State.FAIL;
            boolean curFail = mHeadSavedState.equals(Saved_State.FAIL);
            if (allRequestsFinishSuccess() || curFail) {
                mediatorLiveData.setValue(response);
            }
        };
        Observer<Response<Void, Exception>> trail = response -> {
            mTrailerSavedState = response.isSuccess() ? Saved_State.SUCCESS : Saved_State.FAIL;
            boolean curFail = mTrailerSavedState.equals(Saved_State.FAIL);
            if (allRequestsFinishSuccess() || curFail) {
                mediatorLiveData.setValue(response);
            }
        };
        return saveTractor.combine(saveTrailer, trac, trail, mediatorLiveData);
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

    private enum Saved_State { // Состояния выполнения алгоритма
        NONE, // не запущен (и не будет запущен)
        WAIT, // ожидает запуска
        PROGRESS,  // сохраняется
        SUCCESS, // успешно сохранен
        FAIL // ошибка при сохранении
    }
}
