package com.kolesova_violetta.ltc.datastore;

import android.content.Context;

import androidx.annotation.Size;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.kolesova_violetta.ltc.datastore.device_as_server.DeviceQueries;
import com.kolesova_violetta.ltc.datastore.device_as_server.DeviceServer;

import org.json.JSONObject;

/**
 * Передача/получение данных с датчика
 */
public class Repository {

    private RequestQueue mQueue;

    public Repository(Context context) {
        mQueue = DeviceServer.getInstance(context).getRequestQueue();
    }

    public Repository(RequestQueue queue) {
        mQueue = queue;
    }

    public LiveData<Response<Void, VolleyError>> setRequest(String url) {
        final MutableLiveData<Response<Void, VolleyError>> liveData =
                new MutableLiveData<>();
        StringRequest request = new StringRequest(Request.Method.GET, url,
                success -> {
                    liveData.setValue((Response<Void, VolleyError>) SuccessCb.EMPTY);
                },
                error -> {
                    final FailCallback<Void, VolleyError> a = new FailCallback<>(error);
                    liveData.setValue(a);
                });
        mQueue.add(request);
        return liveData;
    }

    private LiveData<Response<String, VolleyError>> getRequest(String url) {
        final MutableLiveData<Response<String, VolleyError>> liveData =
                new MutableLiveData<>();
        StringRequest request = new StringRequest(Request.Method.GET, url,
                success -> {
                    final SuccessCb<String, VolleyError> a = new SuccessCb<>(success);
                    liveData.setValue(a);
                },
                error -> {
                    error.printStackTrace();
                    final FailCallback<String, VolleyError> a = new FailCallback<>(error);
                    liveData.setValue(a);
                });
        mQueue.add(request);
        return liveData;
    }

    private LiveData<Response<JSONObject, VolleyError>> getRequestJson(String url) {
        final MutableLiveData<Response<JSONObject, VolleyError>> liveData =
                new MutableLiveData<>();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                success -> {
                    final SuccessCb<JSONObject, VolleyError> a = new SuccessCb<>(success);
                    liveData.setValue(a);
                },
                error -> {
                    error.printStackTrace();
                    final FailCallback<JSONObject, VolleyError> a = new FailCallback<>(error);
                    liveData.setValue(a);
                });
        mQueue.add(request);
        return liveData;
    }

    public LiveData<Response<Void, VolleyError>> setDriverName_OnDevice(String name) {
        String url = DeviceQueries.createUrlForDriver(name);
        return setRequest(url);
    }

    public LiveData<Response<Void, VolleyError>> setTractorCalibration_OnDevice(
            @Size(4) float[] coefficients, int steeringAxleWeight, String driverName) {
        String url = DeviceQueries.createUrlForTractor(coefficients, steeringAxleWeight, driverName);
        return setRequest(url);
    }

    public LiveData<Response<Void, VolleyError>> setTrailerCalibration_OnDevice(
            @Size(4) float[] coefficients, String driverName) {
        String url = DeviceQueries.createUrlForTrailer(coefficients, driverName);
        return setRequest(url);
    }

    public LiveData<Response<String, VolleyError>> getHeadConfig_FromDevice() {
        String url = DeviceQueries.getURLWithHeadConfig();
        return getRequest(url);
    }

    public LiveData<Response<String, VolleyError>> getTrailerConfig_FromDevice() {
        String url = DeviceQueries.getURLWithTrailerConfig();
        return getRequest(url);
    }

    public LiveData<Response<JSONObject, VolleyError>> getWeights_FromDevice() {
        String url = DeviceQueries.getURLWithWeights();
        return getRequestJson(url);
    }
}

