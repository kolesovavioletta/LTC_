package com.kolesova_violetta.ltc.datastore.device_as_server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionHelper {

    /**
     * Настройка подключения по заданному IP
     * @param ip IP сервера
     * @return подключение или null
     */
    public static HttpURLConnection openConnection(String ip) {
        try {
            URL url = new URL(ip);
            HttpURLConnection urlConnection =  (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3 * 1000);
            return urlConnection;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
