package com.kolesova_violetta.ltc.datastore.device_as_server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class JsonHelper {
    /**
     * Сбор данных из сети в JSON объект.
     *
     * @param urlConnection подключение к ТД
     * @return любой JSON объект
     */
    public static JSONObject getJSONObjectFromURL(HttpURLConnection urlConnection) {
        BufferedReader br;
        String jsonString;
        StringBuilder sb;
        String line;

        try {
            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();

            jsonString = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
