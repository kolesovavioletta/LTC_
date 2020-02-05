package com.kolesova_violetta.ltc.model.calculations;

import android.util.Log;

import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.exception.DeviceException;
import com.kolesova_violetta.ltc.model.Circuit;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static com.kolesova_violetta.ltc.mock.Const.CIRCUITS_COUNT;

/**
 * Сохранить коэффициенты калибровки устройства на устройство. Т.к. напряжение у устройства
 * переменное, может понадобиться несколько попыток записи. Алгоритм:
 * 1. Посчитать коэффициенты тягача/прицепа
 * 2. Сохранить коэффициенты тягача/прицепа на датчик
 * 3. Дождаться сохранения коэффициентов и тягача, и прицепа (если необходимо)
 * 4. Получить с датчика новые массы
 * 5. Проверить корректность масс. Если массы неверные - повторить.
 */
public class CalcAndSaveCalibrationData {
    private static final int COUNT_TRY_SAVE = 20; // максимальное количество попыток сохранить коэффициенты
    private List<Circuit> mCircuitsTrailer;
    private int mNumberTrySave = 0;

    private Repository mDeviceRepo;

    private List<Circuit> mCircuitsHead; // контура, которые должны быть сохранены
    private String mDriverName;
    private int steeringAxleWeight;
    private boolean correctCoefHead;
    private boolean correctCoefTailer;
    private boolean trailerNeedSave;

    private Function<JSONObject, CompletableSource> checkWeights = jsonObject -> {
        List<Circuit> union = new ArrayList<>(mCircuitsHead);
        if (trailerNeedSave) {
            union.addAll(mCircuitsTrailer);
        }
        boolean correctSaved = CalcAndSaveCalibrationData.this.checkSavedWeights(jsonObject, union);
        return correctSaved ? Completable.complete() : Completable.error(new DeviceException());
    };
    private Predicate<Throwable> retryConditional = e -> {
        if (BuildConfig.DEBUG) {
            Log.d("!@#$", "retry " + mNumberTrySave);
        }
        mNumberTrySave++;
        return e instanceof DeviceException && mNumberTrySave < COUNT_TRY_SAVE;
    };

    public CalcAndSaveCalibrationData(Repository repo, SharedPreferencesRepository localRepo) {
        mDeviceRepo = repo;

        // Подготовка данных для запроса-сохранения данных калибровки
        mDriverName = localRepo.getDriverName();
        steeringAxleWeight = localRepo.getSteeringAxleWeight();
        int modeInstallation = localRepo.getModeInstallation();
        mCircuitsHead = getCircuitsHead(modeInstallation, localRepo);

        trailerNeedSave = modeInstallation == 2 && localRepo.isExistTrailer();
        if (trailerNeedSave) {
            mCircuitsTrailer = localRepo.getCircuitsTrailer();
        }
    }

    public Single<boolean[]> start() {
        Completable saveHead = calcAndSaveHead(mCircuitsHead);
        Completable saveHeadTrailer;
        if (trailerNeedSave) {
            saveHeadTrailer = saveHead.mergeWith(calcAndSaveTrailer(mCircuitsTrailer));
        } else {
            saveHeadTrailer = saveHead;
        }
        return saveHeadTrailer
                .toSingleDefault(new Object())
                .flatMap(o -> mDeviceRepo.getWeights_FromDevice_Json())
                .flatMapCompletable(checkWeights)
                .retry(retryConditional)
                .toSingleDefault(new boolean[]{correctCoefHead, correctCoefTailer});
    }

    /**
     * Расчет коэффициентов для основного датчика и сохранение их на него
     */
    private Completable calcAndSaveHead(List<Circuit> circuits) {
        // Расчет коэффициентов
        CalcHeadCalibrCoefExecutor calcExec = new CalcHeadCalibrCoefExecutor(mDeviceRepo);
        return calcExec.runCalc(circuits)
                .flatMapCompletable(coef -> {
                    correctCoefHead = coefArrWithError(coef);
                    return mDeviceRepo.setTractorCalibration_OnDevice(coef, steeringAxleWeight, mDriverName);
                });
    }

    /**
     * Расчет коэффициентов для дочернего датчика и сохранение их на него
     */
    private Completable calcAndSaveTrailer(List<Circuit> circuits) {
        // Расчет коэффициентов
        CalcTrailerCalibrCoefExecutor calcExec = new CalcTrailerCalibrCoefExecutor(mDeviceRepo);
        return calcExec.runCalc(circuits)
                .flatMapCompletable(coef -> {
                    correctCoefTailer = coefArrWithError(coef);
                    return mDeviceRepo.setTrailerCalibration_OnDevice(coef, mDriverName);
                });
    }

    private boolean coefArrWithError(float[] coef) {
        for (float v : coef) {
            if (v < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSavedWeights(JSONObject deviceWeightsJson, List<Circuit> circuits) {

        final int DELTA_AXLE_WEIGHT = 50; //прогрешность массы оси на весах и с устройства

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

    private List<Circuit> getCircuitsHead(int modeInstallation, SharedPreferencesRepository repo) {
        List<Circuit> circuits = new ArrayList<>(CIRCUITS_COUNT);
        switch (modeInstallation) {
            case 1:
                circuits = repo.getCircuitsTrailer();
                break;
            case 2:
                circuits = repo.getCircuitsTractor();
                break;
            case 3:
            case 4:
                circuits = repo.getCircuitsTrailer();
                circuits.addAll(repo.getCircuitsTractor());
                break;
        }
        return circuits;
    }
}
