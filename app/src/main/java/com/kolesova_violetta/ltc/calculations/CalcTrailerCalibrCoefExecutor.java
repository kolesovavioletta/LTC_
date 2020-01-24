package com.kolesova_violetta.ltc.calculations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.FailCallback;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SuccessCb;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonTrailerResponse;

import java.util.List;

public class CalcTrailerCalibrCoefExecutor extends CalcCalibrCoefExecutor {
    private int U_0 = 1;/*BuildConfig.CALIBRATION_COEF_TRAILER;*/ //АЦП при нулевой нагрузке (при калибровке)

    public CalcTrailerCalibrCoefExecutor(Repository repository) {
        super(repository);
    }

    @Override
    public LiveData<Response<float[], Exception>> runCalc(List<Circuit> circuits) {
        LiveData<Response<String, VolleyError>> response = getRepository().getTrailerConfig_FromDevice();
        return Transformations.map(response, answer -> {
            if (answer.isSuccess()) {
                JsonTrailerResponse json;
                try {
                    SuccessCb<String, VolleyError> s = (SuccessCb<String, VolleyError>) answer;
                    json = new Gson().fromJson(s.getResponse(), JsonTrailerResponse.class);
                    int[] acd = json.getAllAcd();
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
