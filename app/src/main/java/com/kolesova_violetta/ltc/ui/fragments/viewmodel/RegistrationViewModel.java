package com.kolesova_violetta.ltc.ui.fragments.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.VolleyError;
import com.kolesova_violetta.ltc.datastore.CustomData;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory;

public class RegistrationViewModel extends ViewModel {

    private final MutableLiveData<Boolean> progressVisibilityLiveData = new MutableLiveData<>();
    private Repository mRepo;
    private SharedPreferencesRepository mLocalRepo;

    public RegistrationViewModel(@NonNull Repository repository,
                                 SharedPreferencesRepository shPrRepository) {
        mRepo = repository;
        mLocalRepo = shPrRepository;
    }

    public LiveData<Boolean> getProgressVisibilityLiveData() {
        return progressVisibilityLiveData;
    }

    /**
     * Сохранение имени водителя в память телефона и датчика
     */
    public CustomData<Void> execRegistration(String newDriverName) {
        progressVisibilityLiveData.postValue(true);
        return saveDataToDevice(newDriverName);
    }

    public boolean isCorrectName(String name) {
        return (new ConditionalFactory.FullNameConditional().isSuccess(name));
    }

    private CustomData<Void> saveDataToDevice(String name) {
        return mRepo.setDriverName_OnDevice(name)
                .map(x -> {
                    progressVisibilityLiveData.postValue(false);
                    // После обновления данных на датчике можно сохранить имя в телефон
                    if (x.isSuccess()) {
                        saveDataToSharedPreference(name);
                    }
                    return x;
                });
    }

    private void saveDataToSharedPreference(String fio) {
        mLocalRepo.saveDriverName(fio);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private Repository repository;
        private SharedPreferencesRepository sharedPreferencesRepository;

        public Factory(Repository repository, SharedPreferencesRepository sharedPreferencesRepository) {
            this.repository = repository;
            this.sharedPreferencesRepository = sharedPreferencesRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new RegistrationViewModel(repository, sharedPreferencesRepository);
        }
    }
}