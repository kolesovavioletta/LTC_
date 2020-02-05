package com.kolesova_violetta.ltc.ui.fragments.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class RegistrationViewModel extends ViewModel {

    private final MutableLiveData<Boolean> progressVisibilityLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mRegistered = new MutableLiveData<>();

    private Repository mRepo;
    private SharedPreferencesRepository mLocalRepo;

    private CompositeDisposable container = new CompositeDisposable();

    public RegistrationViewModel(@NonNull Repository repository,
                                 SharedPreferencesRepository shPrRepository) {
        mRepo = repository;
        mLocalRepo = shPrRepository;
    }

    public LiveData<Boolean> getProgressVisibilityLiveData() {
        return progressVisibilityLiveData;
    }

    public boolean isCorrectName(String name) {
        return (new ConditionalFactory.FullNameConditional().isSuccess(name));
    }

    /**
     * Сохранение имени водителя в память телефона и датчика
     */
    public void execRegistration(String newDriverName) {
        progressVisibilityLiveData.postValue(true);
        container.add(
                (Disposable) mRepo.saveDriverName_OnDevice(newDriverName)
                        .doFinally(() -> progressVisibilityLiveData.postValue(false))
                        .doOnComplete(() -> {
                            // После обновления данных на датчике можно сохранить имя в телефон
                            saveDataToSharedPreference(newDriverName);
                            mRegistered.setValue(true);
                        })
                        .doOnError(e -> mRegistered.setValue(false))
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        container.clear();
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