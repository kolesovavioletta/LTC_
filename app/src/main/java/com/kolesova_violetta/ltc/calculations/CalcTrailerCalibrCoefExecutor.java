package com.kolesova_violetta.ltc.calculations;

import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.CustomData;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonTrailerResponse;

import java.util.List;

public class CalcTrailerCalibrCoefExecutor extends CalcCalibrCoefExecutor {
    private int U_0 = 1;/*BuildConfig.CALIBRATION_COEF_TRAILER;*/ //АЦП при нулевой нагрузке (при калибровке)

    public CalcTrailerCalibrCoefExecutor(Repository repository) {
        super(repository);
    }

    @Override
    public CustomData<float[]> runCalc(List<Circuit> circuits) {
        CustomData<JsonTrailerResponse> response = getRepository().getTrailerConfig_FromDevice();
        return response.mape(json -> {
            try {
                int[] acd = json.getAllAcd();
                float[] k = calcCoefficients(circuits, U_0, acd);
                return Response.success(k);
            } catch (NullPointerException e) {
                return Response.error(e);
            }
        });
    }
}
