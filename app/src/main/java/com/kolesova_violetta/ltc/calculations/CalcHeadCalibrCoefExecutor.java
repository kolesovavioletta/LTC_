package com.kolesova_violetta.ltc.calculations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.FailCallback;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SuccessCb;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonHeadResponse;

import java.util.List;

public class CalcHeadCalibrCoefExecutor extends CalcCalibrCoefExecutor {
    private int U_0 = 1/*BuildConfig.CALIBRATION_COEF_MASTER*/; //АЦП при нулевой нагрузке (при калибровке)

    public CalcHeadCalibrCoefExecutor(Repository repository) {
        super(repository);
    }

    @Override
    public LiveData<Response<float[], Exception>> runCalc(List<Circuit> circuits) {
        LiveData<Response<String, VolleyError>> response = getRepository().getHeadConfig_FromDevice();
        return Transformations.map(response, answer -> {
            if (answer.isSuccess()) {
                JsonHeadResponse json;
                try {
                    SuccessCb<String, VolleyError> s = (SuccessCb<String, VolleyError>) answer;
                    json = new Gson().fromJson(s.getResponse(), JsonHeadResponse.class);
                    int[] acd = json.getAllHeadAcd();
                    float[] k = calcCoefficients(circuits, U_0, acd);
                    return new SuccessCb<>(k);
                } catch (JsonSyntaxException | NullPointerException e) {
                    return new FailCallback<>(e);
                }
            } else {
                FailCallback<String, VolleyError> s = (FailCallback<String, VolleyError>) answer;
                return new FailCallback<>(s.getError());
            }
        });
    }
}
