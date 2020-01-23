package com.kolesova_violetta.ltc.datastore.device_as_server;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Класс, отправляющий запросы на сервер
 */
public class DeviceServer {
    private static DeviceServer instance = null; // сервер
    private static RequestQueue requestQueue; // пул запросов
    private static Context ctx;

    private DeviceServer(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    /**
     * Получение экземпляра сервера
     * @param context контекст выполнения
     * @return экземпляр сервера
     */
    public static synchronized DeviceServer getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceServer(context);
        }
        return instance;
    }

    /**
     * Получение очереди запросов
     * @return очередь запросов
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Добавление запросов в очередь
     * @param req запрос к серверу
     * @param <T> String, JsonObject, JsonArray
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
