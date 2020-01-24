package com.kolesova_violetta.ltc.calculations;

import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.lifecycle.LiveData;

import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;

import java.util.ArrayList;
import java.util.List;

import static com.kolesova_violetta.ltc.mock.Const.CIRCUITS_COUNT;

public abstract class CalcCalibrCoefExecutor {
    private Repository mRepository;

    CalcCalibrCoefExecutor(Repository repository) {
        mRepository = repository;
    }

    abstract public LiveData<Response<float[], Exception>> runCalc(List<Circuit> circuits);

    float[] calcCoefficients(@NonNull List<Circuit> circuits, @IntRange(from = 1) int U_0,
                             @NonNull @Size(4) int[] acd) {
        if (acd == null || circuits == null || circuits.isEmpty()) {
            return new float[]{0, 0, 0, 0};
        }
        List<Float> coefficientK = new ArrayList<>(CIRCUITS_COUNT);

        Circuit curCirc; // Контур
        String var; // Переменная контура (КонтурX или Прицеп-контурХ)
        int numCirc; // ...
        int iAcd = 0; // ...
        for (int i = 0; i < circuits.size(); i++) {
            curCirc = circuits.get(i);
            var = curCirc.getVar1();
            try {
                //...
                coefficientK.add(curCirc.calc(U_0, acd[iAcd]));
            } catch (ArithmeticException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                coefficientK.add(0f);
            }
            if (curCirc.getType().equals("1")) {
                //...
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d("!@#$", "CalcCalibrCoefExecutor:calcCoefficients:38: " +
                    "ИТОГОВЫЙ массив " + coefficientK.toString());
        }
        return convertListToArrayWithCircuitCountSize(coefficientK);
    }

    @NonNull
    private float[] convertListToArrayWithCircuitCountSize(@NonNull List<Float> floatList) {
        if (floatList.size() > CIRCUITS_COUNT) {
            if (BuildConfig.DEBUG) {
                Log.e("!@#$", "CalcCalibrCoefExecutor:convert:63: array size " + floatList.size());
            }
        }
        float[] floatArray = new float[CIRCUITS_COUNT];
        int i = 0;

        for (Float f : floatList) {
            floatArray[i++] = (f != null ? f : Float.NaN);
        }
        return floatArray;
    }

    Repository getRepository() {
        return mRepository;
    }
}
