package com.kolesova_violetta.ltc.handlers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.VolleyError;
import com.kolesova_violetta.ltc.datastore.FailCallback;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SuccessCb;

public class LiveDataUtils {
    //    @SafeVarargs
//    private final <T> MutableLiveData<Response<T, Exception>> combineLiveData(
//            LiveData<Response<T, Exception>>... liveDatas) {
//        MediatorLiveData<Response<T, Exception>> mediatorLiveData = new MediatorLiveData<>();
//        for (LiveData<Response<T, Exception>> liveData : liveDatas) {
//            mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
//        }
//        return mediatorLiveData;
//    }

    public static <from, to> LiveData<Response<to, Exception>> castErrorLiveData(Response<from, Exception> callback) {
        MutableLiveData<Response<to, Exception>> d = new MutableLiveData<>();
        d.setValue(new FailCallback<>(getError(callback)));
        return d;
    }

    public static <T> LiveData<T> createLiveData(T callback) {
        MutableLiveData<T> d = new MutableLiveData<>();
        d.setValue(callback);
        return d;
    }

    public static <T> Response<T, Exception> upCastLiveData(Response<T, VolleyError> callback) {
        if(callback.isSuccess()) {
            return new SuccessCb<>(getAnswer(callback));
        } else {
            return new FailCallback<>(getError(callback));
        }
    }

    public static <S,E extends Throwable> S getAnswer(Response<S, E> response) {
        if(!response.isSuccess()) {
            throw new ClassCastException("Check response type: response.isSuccess() = false");
        }
        return ((SuccessCb<S, E>) response).getResponse();
    }

    public static <S,E extends Throwable> E getError(Response<S, E> response) {
        if(response.isSuccess()) {
            throw new ClassCastException("Check response type: response.isSuccess() = true");
        }
        return ((FailCallback<S, E>) response).getError();
    }
}
