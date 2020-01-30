package com.kolesova_violetta.ltc.datastore;

import androidx.annotation.Nullable;

import java.util.Arrays;

public class Response<S, F extends Throwable> {

    /**
     * Parsed response, or null in the case of error.
     */
    private final S result;
    /**
     * Detailed error information if <code>errorCode != OK</code>.
     */
    private final F error;

    private boolean success;

    private Response(S result) {
        this.result = result;
        this.error = null;
        success = true;
    }

    private Response(F error) {
        this.result = null;
        this.error = error;
        success = false;
    }

    /**
     * Returns a successful response containing the parsed result.
     */
    public static <S, F extends Throwable> Response<S, F> success(S result) {
        return new Response<>(result);
    }

    /**
     * Returns a failed response containing the given error code and an optional localized message
     * displayed to the user.
     */
    public static <S, F extends Throwable> Response<S, F> error(F error) {
        return new Response<>(error);
    }

    private static String toString(Object val) {
        if (val == null) {
            return "null";
        } else if (val.getClass().isArray()) {
            return deepToString(val);
        } else {
            return val.toString();
        }
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
            //return Arrays.resultEquals((float[]) one, (float[]) two);
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

    /**
     * Returns whether this response is considered successful.
     */
    public boolean isSuccess() {
        return success;
    }

    public S getResult() {
        return result;
    }

    public F getError() {
        return error;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Response<?, ?> cllbck2 = (Response<?, ?>) obj;

        return isSuccess() == cllbck2.isSuccess()
                && resultEquals(result, cllbck2.result) && exceptionEquals(error, cllbck2.error);
    }

    private <T> boolean resultEquals(T value1, @Nullable Object value2) {
        if (value1 == value2) {
            return true;
        }

        if (value1 != null && value2 != null && value1.getClass().equals(value2.getClass())) {
            return value1.getClass().isArray() ? deepEquals(value1, value2) : value1.equals(value2);
        }
        return false;
    }

    private <T> boolean exceptionEquals(T value1, @Nullable Object value2) {
        if (value1 == value2) {
            return true;
        }

        return value1 != null && value2 != null && value1.getClass().equals(value2.getClass());
    }

    @androidx.annotation.NonNull
    @Override
    public String toString() {
        return "Response{" +
                "result=" + toString(result) +
                ", error=" + toString(error) +
                '}';
    }
}
