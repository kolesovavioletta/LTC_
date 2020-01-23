package com.kolesova_violetta.ltc.datastore.device_as_server;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

public class GetWeightFromServer extends AsyncTask<Void, JSONObject, Void> {
    private final Long SLEEP = 1000L; //milliseconds. This is the data refresh period
    private final Long LONG_SLEEP = 10 * 1000L; //10 seconds. This is the data refresh period
    private WeakReference<JSONResponseWeight> activity;

    public GetWeightFromServer(JSONResponseWeight a) {
        activity = new WeakReference<>(a);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            HttpURLConnection h;
            while (!isCancelled() && !Thread.interrupted()) {
                h = ConnectionHelper.openConnection(DeviceQueries.getURLWithWeights());
                JSONObject response = JsonHelper.getJSONObjectFromURL(h);
                publishProgress(response);
                if (!Thread.interrupted()) Thread.sleep(response == null ? LONG_SLEEP : SLEEP);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(JSONObject... object) {
        JSONResponseWeight a = activity.get();

        if (a == null) return;

        if (object == null) a.showMessageNoConnection();
        else a.sendDeviceWeights(object[0]);
    }

    public interface JSONResponseWeight {
        void showMessageNoConnection();

        void sendDeviceWeights(JSONObject weights);
    }
}