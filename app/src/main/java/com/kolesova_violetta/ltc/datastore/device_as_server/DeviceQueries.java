package com.kolesova_violetta.ltc.datastore.device_as_server;

import androidx.annotation.Size;

import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Класс с запросами для сервера
 */
public class DeviceQueries {

    public static String getURLWithWeights() {
        return "";
    }

    public static String getURLWithNetworkConfig() {
        return "";
    }

    public static String getURLWithHeadConfig() {
        return "";
    }

    public static String getURLWithTrailerConfig() {
        return "";
    }

    //-------------------------- TRACTOR -----------------------------------------------------------

    /**
     * Настройка Тягача (постоянные) [2 блока / 1 блок на тягаче] 2, 4
     *
     * @param stateNumber ...
     * @param model       ...
     * @param config      ...
     * @param VIN         ...
     */
    public static String createUrlForTractor(String stateNumber, String model, String config, String VIN,
                                             String name, String wheelFormula, String year) {
        try {
            wheelFormula = URLEncoder.encode(wheelFormula, JsonResponse.CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Новый тягач [1 блок на прицепе] 3
     *
     * @param stateNumber  ...
     * @param model        ...
     * @param config       ...
     * @param wheelFormula
     */
    public static String createUrlForNewTractorTrailerMaster(String stateNumber, String model, String config,
                                                             String wheelFormula, String year) {
        try {
            wheelFormula = URLEncoder.encode(wheelFormula,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";

    }

    /**
     * Сменяющиеся переменные тягача [2 блока / 1 блок на тягаче] 2, 4, 1, 3
     *
     * @param coefficients:      «k1» - коэффициент Контура1 полученный после калибровки
     *                           «k2» - коэффициент Контура2 полученный после калибровки
     *                           «k3» - коэффициент Контура3 полученный после калибровки
     *                           «k4» - коэффициент Контура4 полученный после калибровки
     * @param weightSteeringAxle «steering_axle» - масса рулевой оси если на ней нет контура.
     * @param driverName         «name_calibration» - имя калибровавшего
     *                           «date» - дата калибровки
     */
    public static String createUrlForTractor(@Size(4) float[] coefficients, int weightSteeringAxle, String driverName) {
        return "";
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy|HH:mm");
        DateTime time = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Moscow")));
        return sdf.format(time.getMillis());
    }

    /**
     * Водитель
     *
     * @param nameDriver имя водителя
     */
    public static String createUrlForDriver(String nameDriver) {
        return "";
    }

    //-------------------------- TRAILER -----------------------------------------------------------

    /**
     * Настройка Прицеп (постоянные) [1 блок на прицепе] 1, 3
     *
     * @param stateNumber ...
     * @param model       ...
     * @param config      ...
     * @param VIN         ...
     */
    public static String createUrlForTrailerMaster(String stateNumber, String model, String config,
                                                   String VIN, String year) {
        return "";
    }


    /**
     * Смена и Настройка прицепа (постоянные) [2 блока] 2
     *
     * @param stateNumber ...
     * @param model       ...
     * @param config      ...
     * @param id          ...
     */
    public static String createUrlForTrailerSlave(String stateNumber, String model, String config, String id) {
        return "";
    }

    /**
     * Смена и Настройка прицепа (постоянные) [1 блока на тягаче] 4
     *
     * @param stateNumber ...
     * @param model       ...
     * @param config      ...
     */
    public static String createUrlForTrailerMaster(String stateNumber, String model, String config,
                                                   String year) {
        return "";
    }

    /**
     * Сменяющиеся переменные прицепа [2 блока] 2
     *
     * @param coefficients: «k1» - коэффициент Контура1 полученный после калибровки прицепа (2 блока)
     *                      «k2» - коэффициент Контура2 полученный после калибровки прицепа (2 блока)
     *                      «k3» - коэффициент Контура3 полученный после калибровки прицепа (2 блока)
     *                      «k4» - коэффициент Контура4 полученный после калибровки прицепа (2 блока)
     * @param driverName    «name» - имя калибровавшего
     *                      «date» - дата калибровки
     */
    public static String createUrlForTrailer(@Size(4) float[] coefficients, String driverName) {
        return "";
    }

    //-------------------------- OTHER -----------------------------------------------------------

    /**
     * Сохранение номера телефона, на который отправляются СМС
     *
     * @param phone номер телефона владельца автопарка
     */
    public static String createUrlForPhone(String phone) {
        try {
            phone = URLEncoder.encode(phone, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";

    }
}
