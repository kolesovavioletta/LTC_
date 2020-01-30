package com.kolesova_violetta.ltc.datastore.device_as_server.response;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kolesova_violetta.ltc.datastore.CustomData;

import java.io.UnsupportedEncodingException;

public class GsonRequest<T> extends JsonRequest<T> {
    private final Gson gson = new Gson();
    private final Class<T> clazz;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GsonRequest(String url, Class<T> clazz, CustomData<T> customData) {
        super(Method.GET, url, null, customData, customData);
        this.clazz = clazz;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}
