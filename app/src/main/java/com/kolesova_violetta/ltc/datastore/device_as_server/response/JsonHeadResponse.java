package com.kolesova_violetta.ltc.datastore.device_as_server.response;

import com.google.gson.annotations.SerializedName;

public class JsonHeadResponse extends JsonResponse {
    // конфиг авто ...
    private static final String CONFIG_TRACTOR = "0000000000";
    // конфиг авто ...
    private static final String CONFIG_TRUCK   = "0000000000";

    // масса рулевой оси если на ней нет контура
    @SerializedName("steering_axle")
    private String steeringAxle;

    @SerializedName("name_calibration")
    private String nameCalibration;
    @SerializedName("date")
    private String date;
    @SerializedName("name_driver")
    private String nameDriver;

    // значения АЦП на контурах блока мастер
    @SerializedName("Head-k1")
    private String headAcd1;
    @SerializedName("Head-k2")
    private String headAcd2;
    @SerializedName("Head-k3")
    private String headAcd3;
    @SerializedName("Head-k4")
    private String headAcd4;

    public int getSteeringAxle() {
        return getIntOrZero(steeringAxle);
    }

    public String getNameCalibration() {
        return nameCalibration;
    }

    public String getDate() {
        return date;
    }

    public String getNameDriver() {
        return nameDriver;
    }

    public int getHeadAcd1() {
        return getIntOrZero(headAcd1);
    }

    public int getHeadAcd2() {
        return getIntOrZero(headAcd2);
    }

    public int getHeadAcd3() {
        return getIntOrZero(headAcd3);
    }

    public int getHeadAcd4() {
        return getIntOrZero(headAcd4);
    }

    public int[] getAllHeadAcd() {
        int[] acd = new int[4];
        acd[0] = getHeadAcd1();
        acd[1] = getHeadAcd2();
        acd[2] = getHeadAcd3();
        acd[3] = getHeadAcd4();
        return acd;
    }
}
