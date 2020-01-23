package com.kolesova_violetta.ltc.ui.fragments.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.VolleyError;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory;

public class RegistrationViewModel extends ViewModel {

    private Repository mRepo;
    private SharedPreferencesRepository mLocalRepo;

    private final MutableLiveData<Boolean> progressVisibilityLiveData = new MutableLiveData<>();

    public RegistrationViewModel(@NonNull Repository repository,
                                 SharedPreferencesRepository shPrRepository) {
        mRepo = repository;
        mLocalRepo = shPrRepository;
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

    public LiveData<Boolean> getProgressVisibilityLiveData() {
        return progressVisibilityLiveData;
    }

    /**
     * Сохранение имени водителя в память телефона и датчика
     */
    public LiveData<Response<Void, VolleyError>> execRegistration(String newDriverName) {
        progressVisibilityLiveData.postValue(true);
        //saveDataToSharedPreference(newDriverName);
        return saveDataToDevice(newDriverName);
    }

    public boolean isCorrectName(String name) {
        return (new ConditionalFactory.FullNameConditional().isSuccess(name));
    }

    private LiveData<Response<Void, VolleyError>> saveDataToDevice(String name) {
        LiveData<Response<Void, VolleyError>> response = mRepo.setDriverName_OnDevice(name);
        return Transformations.map(response, x ->{
            progressVisibilityLiveData.postValue(false);
            // После обновления данных на датчике можно сохранить имя в телефон
            if(x.isSuccess()) {
                saveDataToSharedPreference(name);
            }
            return x;
        });
    }

    private void saveDataToSharedPreference(String fio) {
        mLocalRepo.saveDriverName(fio);
    }
}