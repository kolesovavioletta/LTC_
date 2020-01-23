package com.kolesova_violetta.ltc.datastore;

import androidx.annotation.NonNull;

import java.util.Arrays;

// Success Callback
public class SuccessCb<S, F extends Throwable> extends Response<S, F> {

    public static SuccessCb<Void, ? extends Throwable> EMPTY = new SuccessCb<>();

    private S response;

    public SuccessCb() {
        success = true;
    }

    public SuccessCb(S response) {
        this();
        setResponse(response);
    }

    private static String deepToString(Object one) {
        if (one instanceof byte[])
            return Arrays.toString((byte[]) one);
        if (one instanceof char[])
            return Arrays.toString((char[]) one);
        if (one instanceof short[])
            return Arrays.toString((short[]) one);
        if (one instanceof int[])
            return Arrays.toString((int[]) one);
        if (one instanceof long[])
            return Arrays.toString((long[]) one);
        if (one instanceof boolean[])
            return Arrays.toString((boolean[]) one);
        if (one instanceof float[])
            return Arrays.toString((float[]) one);
        if (one instanceof double[])
            return Arrays.toString((double[]) one);
        if (one instanceof Object[])
            return Arrays.toString((Object[]) one);
        return one.toString();
    }

    private static boolean deepEquals(Object one, Object two) {
        if (one instanceof byte[])
            return Arrays.equals((byte[]) one, (byte[]) two);
        if (one instanceof char[])
            return Arrays.equals((char[]) one, (char[]) two);
        if (one instanceof short[])
            return Arrays.equals((short[]) one, (short[]) two);
        if (one instanceof int[])
            return Arrays.equals((int[]) one, (int[]) two);
        if (one instanceof long[])
            return Arrays.equals((long[]) one, (long[]) two);
        if (one instanceof boolean[])
            return Arrays.equals((boolean[]) one, (boolean[]) two);
        if (one instanceof float[]) {
            //return Arrays.equals((float[]) one, (float[]) two);
            return equals((float[]) one, (float[]) two);
        }
        if (one instanceof double[])
            return Arrays.equals((double[]) one, (double[]) two);
        if (one instanceof Object[])
            return Arrays.equals((Object[]) one, (Object[]) two);
        return one.equals(two);
    }

    public static boolean equals(float[] a, float[] a2) {
        if (a == a2)
            return true;
        if (a == null || a2 == null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i = 0; i < length; i++)
            if (Math.abs(a[i] - a2[i]) > 0.001)
                return false;

        return true;
    }

    public S getResponse() {
        return response;
    }

    public void setResponse(S response) {
        this.response = response;
    }

    @NonNull
    @Override
    public String toString() {
        if (response != null) {
            if (response.getClass().isArray()) {
                return deepToString(response);
            } else {
                return response.toString();
            }
        } else {
            return "empty response";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SuccessCb<?, ?> cllbck2 = (SuccessCb<?, ?>) obj;

        if (response.getClass().isArray() ^ cllbck2.response.getClass().isArray()) {
            return response.equals(cllbck2.response);
        } else {
            return deepEquals(response, cllbck2.response);
        }
    }
}
