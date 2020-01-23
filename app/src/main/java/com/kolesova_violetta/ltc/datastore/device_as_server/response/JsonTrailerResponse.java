package com.kolesova_violetta.ltc.datastore.device_as_server.response;

import com.google.gson.annotations.SerializedName;

public class JsonTrailerResponse extends JsonResponse {
    // конфиг авто ...
    private static final String CONFIG_TRAILER = "0000000000";

    // значения АЦП0 на контурах блока прицеп
    @SerializedName("Trailer-k1")
    private String acd1;
    @SerializedName("Trailer-k2")
    private String acd2;
    @SerializedName("Trailer-k3")
    private String acd3;
    @SerializedName("Trailer-k4")
    private String acd4;

    public int getAcd1() {
        return getIntOrZero(acd1);
    }

    public int getAcd2() {
        return getIntOrZero(acd2);
    }

    public int getAcd3() {
        return getIntOrZero(acd3);
    }

    public int getAcd4() {
        return getIntOrZero(acd4);
    }

    public int[] getAllAcd() {
        int[] acd = new int[4];
        acd[0] = getAcd1();
        acd[1] = getAcd2();
        acd[2] = getAcd3();
        acd[3] = getAcd4();
        return acd;
    }
}
