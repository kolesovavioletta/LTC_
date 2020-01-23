package com.kolesova_violetta.ltc.datastore.device_as_server.response;

import java.io.UnsupportedEncodingException;

public abstract class JsonResponse {
    public static final String CHARSET_NAME = "UTF-8";

    protected int getIntOrZero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected float getFloatOrZero(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    protected String decodeString(String value) {
        if (value == null) return value;

        String decoded;
        try {
            decoded = new String(value.getBytes("ISO-8859-1"), CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            decoded = value;
        }
        return decoded;
    }
}
