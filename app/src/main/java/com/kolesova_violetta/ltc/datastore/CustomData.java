package com.kolesova_violetta.ltc.datastore;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;

public class CustomData<S> extends MediatorLiveData<Response<S, Exception>>
        implements com.android.volley.Response.Listener<S>, com.android.volley.Response.ErrorListener {

    @Override
    public void onResponse(S response) {
        setValue(Response.success(response));
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setValue(Response.error(error));
    }

    public void onErrorResponse(Exception error) {
        setValue(Response.error(error));
    }

    public static <T> CustomData<T> getInstance(T val) {
        CustomData<T> customData = new CustomData<>();
        customData.onResponse(val);
        return customData;
    }

    public static <S, F extends Exception> CustomData<S> getInstance(F val) {
        CustomData<S> customData = new CustomData<>();
        customData.onErrorResponse(val);
        return customData;
    }

    public <TO> CustomData<TO> map(@NonNull final MapFun<S, TO> mapFunction) {
        final CustomData<TO> result = new CustomData<>();
        result.addSource(this, x -> {
            Response<TO, Exception> y = mapFunction.apply(x);
            result.setValue(y);
        });
        return result;
    }

    public <TO> CustomData<TO> mape(@NonNull final MapFunErr<S, TO> mapFunction) {
        return map(answer -> {
            if (answer.isSuccess()) {
                return mapFunction.apply(answer.getResult());
            } else {
                return Response.error(answer.getError());
            }
        });
    }

    public <Y> CustomData<Y> switchMap(@NonNull final SwitchMapFun<S, Y> switchMapFunction) {
        final CustomData<Y> result = new CustomData<>();
        result.addSource(this, new Observer<Response<S, Exception>>() {
            CustomData<Y> mSource;

            @Override
            public void onChanged(Response<S, Exception> x) {
                CustomData<Y> newLiveData = switchMapFunction.apply(x);
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        return result;
    }

    /**
     * Объединение двух источников данных.
     */
    private CustomData<S> combine(CustomData<S> liveData2) {
        CustomData<S> liveDataMerger = new CustomData<>();
        Observer<Response<S, Exception>> obs = liveDataMerger::setValue;
        return combine(liveData2, obs, obs, liveDataMerger);
    }

    public CustomData<S> combine(CustomData<S> liveData2, Observer<Response<S, Exception>> obs1,
                                 Observer<Response<S, Exception>> obs2, CustomData<S> liveDataMerger) {
        liveDataMerger.addSource(this, obs1);
        liveDataMerger.addSource(liveData2, obs2);
        return liveDataMerger;
    }

    public interface MapFun<FROM, TO> {
        Response<TO, Exception> apply(Response<FROM, Exception> val);
    }

    public interface MapFunErr<FROM, TO> {
        Response<TO, Exception> apply(FROM val);
    }

    public interface SwitchMapFun<FROM, TO> {
        CustomData<TO> apply(Response<FROM, Exception> val);
    }
}
