package com.kolesova_violetta.ltc.datastore;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.Circuit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kolesova_violetta.ltc.mock.Const.*;
import static com.kolesova_violetta.ltc.ui.fragments.CalibrationPreferenceFragment.PREF_AXLE_TRACTOR;
import static com.kolesova_violetta.ltc.ui.fragments.CalibrationPreferenceFragment.PREF_AXLE_TRAILER;
import static com.kolesova_violetta.ltc.ui.fragments.CalibrationPreferenceFragment.PREF_CALIBRATION_DATE;
import static com.kolesova_violetta.ltc.ui.fragments.CalibrationPreferenceFragment.PREF_CALIBRATION_DRIVER_NAME;
import static com.kolesova_violetta.ltc.ui.fragments.CalibrationPreferenceFragment.PREF_WEIGHT_AXLE_UNDER_CAB;


/**
 * Сохранение и получение данных из SharedPreferences о машине, водителе, мониторинге.
 */
public class SharedPreferencesRepository {

    private SharedPreferences mShPref;

    public SharedPreferencesRepository(Context context) {
        mShPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getString(String preferenceKey, String defValue) {
        return mShPref.getString(preferenceKey, defValue);
    }

    private void saveString(String preferenceId, String value) {
        mShPref.edit()
                .putString(preferenceId, value)
                .apply();
    }

    private String setValue(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    public void saveDriverName(String name) {
        saveString(PREF_DRIVER_NAME, name);
    }

    public String getDriverName() {
        return mShPref.getString(PREF_DRIVER_NAME, "");
    }

    public void setTrailer(boolean on) {
        mShPref.edit()
                .putBoolean(PREF_TRAILER_SWITCH, on)
                .apply();
    }
    /**
     * Тягач-Slave сохраняется также {@link ***}
    */
     private void saveTractor(String stateNumber, String mark, String model, String vin,
                             String wheelFormula, String year) {
        mShPref.edit()
                .putString(PREF_TRACTOR_NUMBER, setValue(stateNumber))
                .putString(PREF_TRACTOR_MARK, setValue(mark))
                .putString(PREF_TRACTOR_MODEL, setValue(model))
                .putString(PREF_TRACTOR_VIN, setValue(vin))
                .putString(PREF_TRACTOR_WHEEL_FORMULA, setValue(wheelFormula))
                .putString(PREF_TRACTOR_YEAR, setValue(year))
                .apply();
    }

    private void saveTrailer(String stateNumber, String model, String mark, String vin, String year) {
        mShPref.edit()
                .putString(PREF_TRAILER_NUMBER, setValue(stateNumber))
                .putString(PREF_TRAILER_MARK, setValue(mark))
                .putString(PREF_TRAILER_TYPE, setValue(model))
                .putString(PREF_TRAILER_VIN, setValue(vin))
                .putString(PREF_TRAILER_YEAR, setValue(year))
                .apply();
    }

    /**
     * Получение количества контуров для тягача или прицепа
     * @param prefName PREF_TRACTOR_CIRCUITS / PREF_TRAILER_CIRCUITS
     * @param offsetForIndexPrefType START_POSITION_PREF_TYPE_TRACTOR (1) / START_POSITION_PREF_TYPE_TRAILER (5)
     * @return количество контуров
     */
    private int getCountCircuits(String prefName, int offsetForIndexPrefType) {
        // circuitsCount - количество контуров, введенное пользователем
        String sCircuitsCount = mShPref.getString(prefName, "0");
        if (sCircuitsCount.isEmpty()) sCircuitsCount = "0";
        int iCircuitsCount = Integer.parseInt(sCircuitsCount);
        // circuitsOrPairCircuitsCount - количество двусторонних контуров и пар односторонних контуров
        int circuitsOrPairCircuitsCount = iCircuitsCount;
        String typeUnilateral = "1";

        for (int i = 0; i < iCircuitsCount; i++) {
            //The creating of the name of the current circuit setting (type of circuit)
            String name = PREF_TYPE.concat(String.valueOf(i + offsetForIndexPrefType));
            String type = mShPref.getString(name, "2");

            if (!type.isEmpty() && type.equals(typeUnilateral)) {
                --circuitsOrPairCircuitsCount;
            }
        }
        return circuitsOrPairCircuitsCount;
    }

    public void saveCalibration(String[] weightsTractor, String[] weightsTrailer, String driverName,
                                String dateTime) {
        SharedPreferences.Editor editor = mShPref.edit();
        saveAxesWeights(weightsTractor, PREF_AXLE_TRACTOR, editor);
        saveAxesWeights(weightsTrailer, PREF_AXLE_TRAILER, editor);
        editor.putString(PREF_CALIBRATION_DRIVER_NAME, driverName);
        editor.putString(PREF_CALIBRATION_DATE, dateTime);
        editor.apply();
    }

    private void saveAxesWeights(final String[] weights, final String prefKey, SharedPreferences.Editor editor) {
        String key;
        for(int i = 0 ; i < weights.length; i++ ) {
            key = prefKey + (i+1) + "_et";
            editor.putString(key, weights[i]);
        }
    }

    class CircuitShPref extends Circuit {
        private int indexCircuit;

        CircuitShPref(final int indexCircuit, final int indexAxle,
                      final String nameAxlePrefOfTractorOrTrailer) {
            this.indexCircuit = indexCircuit;
            loadType();
            loadVar();
            loadAxesCount();
            loadWeight(getAxesCount(), indexAxle, nameAxlePrefOfTractorOrTrailer);
        }

        private void loadType() {
            setType(mShPref.getString(PREF_TYPE.concat(String.valueOf(indexCircuit)), "2"));
        }

        private void loadVar() {
            if(getType().equals("2")) {
                setVar1(mShPref.getString(PREF_VARIABLE.concat(String.valueOf(indexCircuit)), ERROR_VALUE));
            } else {
                setVar1(mShPref.getString(PREF_VARIABLE_LEFT.concat(String.valueOf(indexCircuit)), ERROR_VALUE));
                setVar2(mShPref.getString(PREF_VARIABLE_RIGHT.concat(String.valueOf(indexCircuit)), ERROR_VALUE));
            }
        }

        // загрузка масс осей и суммарной массы осей контура без груза
        private void loadWeight(int countAxes, int indexAxleRepo, String nameAxlePrefOfTractorOrTrailer) {
            int sum = 0;
            int endIndex = indexAxleRepo + countAxes;
            int weightAxle;
            int[] weightAxes = new int[countAxes];
            for (int i = indexAxleRepo, indexAxleInCirc = 0; i < endIndex; i++, indexAxleInCirc++) {
                weightAxle = getIntWeightAxle(nameAxlePrefOfTractorOrTrailer, i);
                if(BuildConfig.DEBUG) {
                    Log.d("!@#$", "CircuitShPref:loadWeight:271: ось " + (i) + " масса " + weightAxle);
                }
                weightAxes[indexAxleInCirc] = weightAxle;
                sum += weightAxle;
            }
            if(BuildConfig.DEBUG) {
                Log.d("!@#$", "CircuitShPref:loadWeight:283: контур масса " + sum);
            }
            setWeight(sum);
            setWeightAxes(weightAxes);
        }

        private void loadAxesCount() {
            String sAxes = mShPref.getString(PREF_AXES.concat(String.valueOf(indexCircuit)), "0");
            if (sAxes.isEmpty()) sAxes = "0";
            int iAxes;
            try {
                iAxes = Integer.parseInt(sAxes);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                iAxes = 0;
            }
            setAxesCount(iAxes);
        }
    }

    private int getIntWeightAxle(final String nameAxlePrefOfTractorOrTrailer, final int i) {
        String w = getStrWeightAxle(nameAxlePrefOfTractorOrTrailer, i);
        int iW;
        try {
            iW = Integer.valueOf(w);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            iW = 0;
        }
        return iW;
    }

    private String getStrWeightAxle(final String nameAxlePrefOfTractorOrTrailer, final int i) {
        String w = mShPref.getString(nameAxlePrefOfTractorOrTrailer + i + "_et", "0");
        if (w.isEmpty()) w = "0";
        return w;
    }

    public List<Circuit> getCircuitsTractor() {
        int iCircStart = START_POSITION_PREF_TYPE_TRACTOR;
        int circCount = getCountCircuits(PREF_TRACTOR_CIRCUITS, START_POSITION_PREF_TYPE_TRACTOR);
        int iCircEnd = iCircStart + circCount;
        int iAxes = isExistCircUnderCab() ? 1 : 2;
        List<Circuit> circuits = new ArrayList<>();
        for (int iCirc = iCircStart; iCirc < iCircEnd; iCirc++) {
            if(BuildConfig.DEBUG) {
                Log.d("!@#$", "SharedPreferencesRepository:getWeightsTractor:306: " + iCirc);
            }
            CircuitShPref c = new CircuitShPref(iCirc, iAxes, NAME_PREF_AXLE_TRACTOR);
            circuits.add(c);
            iAxes += c.getAxesCount();
        }
        return circuits;
    }

    public List<Circuit> getCircuitsTrailer() {
        if(!isExistTrailer()) {
            return Collections.EMPTY_LIST;
        }

        int iCircStart = START_POSITION_PREF_TYPE_TRAILER;
        int circCount = getCountCircuits(PREF_TRAILER_CIRCUITS, START_POSITION_PREF_TYPE_TRAILER);
        int iCircEnd = iCircStart + circCount;
        List<Circuit> circuits = new ArrayList<>();
        for (int iCirc = iCircStart, iAxes = 1; iCirc < iCircEnd; iCirc++) {
            if(BuildConfig.DEBUG) {
                Log.d("!@#$", "SharedPreferencesRepository:getWeightsTrailer:319: " + iCirc);
            }
            CircuitShPref c = new CircuitShPref(iCirc, iAxes, NAME_PREF_AXLE_TRAILER);
            circuits.add(c);
            iAxes += c.getAxesCount();
        }
        return circuits;
    }

    public String[] getWeightsAxes(String nameAxlePrefOfTractorOrTrailer, int axesCount) {
        String[] weightAxes = new String[axesCount];
        for (int i = 0; i < axesCount; i++) {
            weightAxes[i] = getStrWeightAxle(nameAxlePrefOfTractorOrTrailer, i);
        }
        return weightAxes;
    }

    public String[] getWeightsAxesTractor(int axesCount) {
        return getWeightsAxes(PREF_AXLE_TRACTOR, axesCount);
    }

    public String[] getWeightsAxesTrailer(int axesCount) {
        return getWeightsAxes(PREF_AXLE_TRAILER, axesCount);
    }

    public boolean isExistTrailer() {
        return mShPref.getBoolean(PREF_TRAILER_SWITCH, false);
    }

    public int getModeInstallation() {
        return Integer.parseInt(mShPref.getString(PREF_TYPE_INSTALLATION, "2"));
    }

    public int getSteeringAxleWeight() {
        return Integer.parseInt(mShPref.getString(PREF_WEIGHT_AXLE_UNDER_CAB, "0"));
    }

    public boolean isExistCircUnderCab() {
        return mShPref.getBoolean(PREF_AXLE_UNDER_CAB, false);
    }

    public int getAxesTractorCount() {
        return 0;
    }

    public int getAxesTrailerCount() {
        return 0;
    }
}
