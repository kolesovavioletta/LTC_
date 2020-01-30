package com.kolesova_violetta.ltc.calculations;

import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.CustomData;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonHeadResponse;

import java.util.List;

public class CalcHeadCalibrCoefExecutor extends CalcCalibrCoefExecutor {
    private int U_0 = 1/*BuildConfig.CALIBRATION_COEF_MASTER*/; //АЦП при нулевой нагрузке (при калибровке)

    public CalcHeadCalibrCoefExecutor(Repository repository) {
        super(repository);
    }

    @Override
    public CustomData<float[]> runCalc(List<Circuit> circuits) {
        CustomData<JsonHeadResponse> response = getRepository().getHeadConfig_FromDevice();
        return response.mape(json -> {
            try {
                int[] acd = json.getAllHeadAcd();
                float[] k = calcCoefficients(circuits, U_0, acd);
                return Response.success(k);
            } catch (NullPointerException e) {
                return Response.error(e);
            }
        });
    }
}
