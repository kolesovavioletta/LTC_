package com.kolesova_violetta.ltc.ui.fragments.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Response;
import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.handlers.BaseSchedulerProvider;
import com.kolesova_violetta.ltc.model.Circuit;
import com.kolesova_violetta.ltc.mock.SmsSenderAfterCalibration;
import com.kolesova_violetta.ltc.mock.TractorCache;
import com.kolesova_violetta.ltc.model.calculations.CalcAndSaveCalibrationData;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.datastore.device_as_server.DeviceQueries;

import java.util.Arrays;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class CalibrationViewModel extends ViewModel {

    private int mAxesTractorCount;
    private int mAxesTrailerCount;

    private Repository mRepo;
    private SharedPreferencesRepository mLocalRepo;
    private BaseSchedulerProvider mSchedulers;

    private MutableLiveData<String> mSensorErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mCoefSaved = new MutableLiveData<>();

    private CompositeDisposable container = new CompositeDisposable();

    public CalibrationViewModel(@NonNull Repository repository,
                                SharedPreferencesRepository shPrRepository,
                                BaseSchedulerProvider schedulerProvider) {
        mRepo = repository;
        mLocalRepo = shPrRepository;
        mSchedulers = schedulerProvider;

        mAxesTractorCount = mLocalRepo.getAxesTractorCount();
        mAxesTrailerCount = mLocalRepo.getAxesTrailerCount();
    }

    public LiveData<String> getSensorErrorLiveData() {
        return mSensorErrorLiveData;
    }

    public LiveData<Boolean> getCoefSaved() {
        return mCoefSaved;
    }

    /**
     * Расчитать и сохранить коэффициенты для калибровки датчиков
     */
    public void onEndInputWeights(String[] weightsTractor, String[] weightsTrailer) {
        // Сохранение локально
        String driverName = getDriverNameAfterSuccessCalibration();
        String dateTime = getDatetimeAfterSuccessCalibration();
        mLocalRepo.saveCalibration(weightsTractor, weightsTrailer, driverName, dateTime);
        // Сохранение на датчик
        container.add(makeCalcAndSaveCalibr(mRepo, mLocalRepo).start()
                .subscribeOn(mSchedulers.computation())
                .observeOn(mSchedulers.ui())
                .subscribe(deviceWorkCorrect -> {
                    String errSensor = createErrSensorString(deviceWorkCorrect);
                    if (!errSensor.isEmpty()) {
                        mSensorErrorLiveData.postValue(errSensor);
                    }
                    mCoefSaved.postValue(true);
                }, e -> {
                    mCoefSaved.postValue(false);
                })
        );
    }

    CalcAndSaveCalibrationData makeCalcAndSaveCalibr(Repository repo, SharedPreferencesRepository shRepo) {
        return new CalcAndSaveCalibrationData(repo, shRepo);
    }

    @Override
    protected void onCleared() {
        container.clear();
        super.onCleared();
    }

    // {true, true} -> "1, 2"
    // {false, false} -> ""
    // {true, false} -> "1"
    // {false, true} -> "2"
    private String createErrSensorString(@Size(2) boolean[] numberOfDevice) {
        if (numberOfDevice == null || numberOfDevice.length != 2) {
            String msg = "CalibrationViewModel:createErrSensorString:96: " +
                    "массив о состоянии датчиков некорректен " + Arrays.toString(numberOfDevice);
            if (BuildConfig.DEBUG) {
                throw new ArrayIndexOutOfBoundsException(msg);
            } else {
                Log.e("!@#$", msg);
                return "";
            }
        }
        boolean one = numberOfDevice[0];
        boolean two = numberOfDevice[1];
        if (one & two) { // true, true
            return "1, 2";
        } else if (one) {
            return "1";
        } else if (two) {
            return "2";
        } else { // false, false
            return "";
        }
    }

    public void sendSmsAfterCalibration(Context context) {
        List<Circuit> circuits = mLocalRepo.getCircuitsTractor();
        circuits.addAll(mLocalRepo.getCircuitsTrailer());
        new SmsSenderAfterCalibration(context).send(circuits);
    }

    public String getDatetimeAfterSuccessCalibration() {
        return DeviceQueries.getCurrentDateTime();
    }

    public String getDriverNameAfterSuccessCalibration() {
        return TractorCache.getDriverName();
    }

    public String[] getWeightsTractor() {
        return mLocalRepo.getWeightsAxesTractor(mAxesTractorCount);
    }

    public String[] getWeightsTrailer() {
        return mLocalRepo.getWeightsAxesTrailer(mAxesTrailerCount);
    }

    public int getAxesTractorCount() {
        return mAxesTractorCount;
    }

    public int getAxesTrailerCount() {
        return mAxesTrailerCount;
    }

    public String getPreferenceValue(String key, String def) {
        return mLocalRepo.getString(key, def);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private Repository repository;
        private SharedPreferencesRepository sharedPreferencesRepository;
        private BaseSchedulerProvider mSchedulers;

        public Factory(Repository repository,
                       SharedPreferencesRepository sharedPreferencesRepository,
                       BaseSchedulerProvider schedulers) {
            this.repository = repository;
            this.sharedPreferencesRepository = sharedPreferencesRepository;
            this.mSchedulers = schedulers;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CalibrationViewModel(repository, sharedPreferencesRepository, mSchedulers);
        }
    }
}
