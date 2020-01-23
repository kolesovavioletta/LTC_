package com.kolesova_violetta.ltc.handlers;

import java.util.Date;

public class TimeHelper {
    static final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

    public static Date getDateByHoursAndMinutes(int hours, int minutes) {
        minutes += hours*60;
        return new Date( minutes * ONE_MINUTE_IN_MILLIS);
    }

    public static int parseHour(String value) {
        try {
            String[] time = value.split(":");
            return (Integer.parseInt(time[0]));
        } catch (Exception e) {
            return 0;
        }
    }

    public static int parseMinute(String value) {
        try {
            String[] time = value.split(":");
            return (Integer.parseInt(time[1]));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String timeToString(int h, int m) {
        return String.format("%02d", h) + ":" + String.format("%02d", m);
    }
}
