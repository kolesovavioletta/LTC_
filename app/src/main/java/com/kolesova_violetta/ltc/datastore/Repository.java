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

    public CustomData<Void> setRequest(String url) {
        final CustomData<String> liveData = new CustomData<>();
        StringRequest request = new StringRequest(Request.Method.GET, url, liveData, liveData);
        mQueue.add(request);
        return liveData.mape(val -> Response.success(null));
    }

    private CustomData<JSONObject> getRequestJson(String url) {
        final CustomData<JSONObject> liveData = new CustomData<>();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                liveData, liveData);
        mQueue.add(request);
        return liveData;
    }

    private <T> CustomData<T> getRequestJson(String url, Class<T> clazz) {
        CustomData<T> data = new CustomData<>();
        GsonRequest<T> request = new GsonRequest<>(url, clazz, data);
        mQueue.add(request);
        return data;
    }

    public CustomData<Void> setDriverName_OnDevice(String name) {
        String url = DeviceQueries.createUrlForDriver(name);
        return setRequest(url);
    }

    public CustomData<Void> setTractorCalibration_OnDevice(
            @Size(4) float[] coefficients, int steeringAxleWeight, String driverName) {
        String url = DeviceQueries.createUrlForTractor(coefficients, steeringAxleWeight, driverName);
        return setRequest(url);
    }

    public CustomData<Void> setTrailerCalibration_OnDevice(
            @Size(4) float[] coefficients, String driverName) {
        String url = DeviceQueries.createUrlForTrailer(coefficients, driverName);
        return setRequest(url);
    }

    public CustomData<JsonHeadResponse> getHeadConfig_FromDevice() {
        String url = DeviceQueries.getURLWithHeadConfig();
        return getRequestJson(url, JsonHeadResponse.class);
    }

    public CustomData<JsonTrailerResponse> getTrailerConfig_FromDevice() {
        String url = DeviceQueries.getURLWithTrailerConfig();
        return getRequestJson(url, JsonTrailerResponse.class);
    }

    public CustomData<JSONObject> getWeights_FromDevice() {
        String url = DeviceQueries.getURLWithWeights();
        return getRequestJson(url);
    }
}

