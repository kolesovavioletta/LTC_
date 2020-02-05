package com.kolesova_violetta.ltc.datastore;

import android.content.Context;

import androidx.annotation.Size;
import androidx.lifecycle.LiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.kolesova_violetta.ltc.datastore.device_as_server.DeviceQueries;
import com.kolesova_violetta.ltc.datastore.device_as_server.DeviceServer;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.GsonRequest;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonHeadResponse;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonTrailerResponse;

import org.json.JSONObject;

import io.reactivex.Completable;
import io.reactivex.Single;

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

    private Single<JSONObject> getRequestJsonObject(String url) {
        return Single.create(e -> {
            JsonObjectRequest request =
                    new JsonObjectRequest(Request.Method.GET, url, null, e::onSuccess, e::onError);
            mQueue.add(request);
        });
    }

    private <T> Single<T> getRequestJsonResponse(String url, Class<T> clazz) {
        return Single.create(e -> {
            GsonRequest<T> request = new GsonRequest<>(url, clazz, e::onSuccess, e::onError);
            mQueue.add(request);
        });
    }

    private Single<String> getRequestString(String url) {
        return Single.create(e -> {
            StringRequest request = new StringRequest(Request.Method.GET, url, e::onSuccess, e::onError);
            mQueue.add(request);
        });
    }

    private Completable setRequest(String url) {
        return Completable.create(e -> {
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    x -> e.onComplete(), e::onError);
            mQueue.add(request);
        });
    }


    //----------------------------------------------------------------------------------------------


    public Completable saveDriverName_OnDevice(String name) {
        String url = DeviceQueries.createUrlForDriver(name);
        return setRequest(url);
    }

    public Completable setTractorCalibration_OnDevice(
            @Size(4) float[] coefficients, int steeringAxleWeight, String driverName) {
        String url = DeviceQueries.createUrlForTractor(coefficients, steeringAxleWeight, driverName);
        return setRequest(url);
    }

    public Completable setTrailerCalibration_OnDevice(
            @Size(4) float[] coefficients, String driverName) {
        String url = DeviceQueries.createUrlForTrailer(coefficients, driverName);
        return setRequest(url);
    }

    public Single<JsonHeadResponse> getHeadConfig_FromDevice() {
        String url = DeviceQueries.getURLWithHeadConfig();
        return getRequestJsonResponse(url, JsonHeadResponse.class);
    }

    public Single<JsonTrailerResponse> getTrailerConfig_FromDevice() {
        String url = DeviceQueries.getURLWithTrailerConfig();
        return getRequestJsonResponse(url, JsonTrailerResponse.class);
    }

    public Single<JSONObject> getWeights_FromDevice_Json() {
        String url = DeviceQueries.getURLWithWeights();
        return getRequestJsonObject(url);
    }
}

