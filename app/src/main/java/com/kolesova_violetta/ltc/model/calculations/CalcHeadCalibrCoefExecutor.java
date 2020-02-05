package com.kolesova_violetta.ltc.model.calculations;

import com.android.volley.Response;
import com.kolesova_violetta.ltc.model.Circuit;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonHeadResponse;

import java.util.List;

import io.reactivex.Single;

public class CalcHeadCalibrCoefExecutor extends CalcCalibrCoefExecutor {
    private int U_0 = 1/*BuildConfig.CALIBRATION_COEF_MASTER*/; //АЦП при нулевой нагрузке (при калибровке)

    public CalcHeadCalibrCoefExecutor(Repository repository) {
        super(repository);
    }

    @Override
    public Single<float[]> runCalc(List<Circuit> circuits) {
        return getRepository().getHeadConfig_FromDevice()
                .map(json -> {
                    int[] acd = json.getAllHeadAcd();
                    return calcCoefficients(circuits, U_0, acd);
                });
    }
}
