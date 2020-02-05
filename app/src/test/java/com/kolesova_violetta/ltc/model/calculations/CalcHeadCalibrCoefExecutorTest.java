package com.kolesova_violetta.ltc.model.calculations;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonHeadResponse;
import com.kolesova_violetta.ltc.handlers.Converters;
import com.kolesova_violetta.ltc.model.Circuit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;

import static org.mockito.Mockito.when;

/**
 * Тесты ориентированы на оба класса {@link CalcHeadCalibrCoefExecutor}{@link CalcTrailerCalibrCoefExecutor}
 */
public class CalcHeadCalibrCoefExecutorTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Repository rRepo;

    private CalcHeadCalibrCoefExecutor calc;

    private List<Circuit> circuit;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        calc = new CalcHeadCalibrCoefExecutor(rRepo);
        circuit = new ArrayList<>();
        circuit.add(new Circuit(2, "2", "A1", 300));
        circuit.add(new Circuit(2, "2", "A2", 600));
        circuit.add(new Circuit(2, "1", "A3", "A4", 1400));
    }

    @After
    public void tearDown() throws Exception {
        circuit = null;
        calc = null;
    }

    @Test
    public void calc_Correct() {
        JsonHeadResponse jsonHeadResponse = new JsonHeadResponse();
        jsonHeadResponse.setAllHeadAcd("5000", "10000", "15000", "20000");
        Single<JsonHeadResponse> headResponseCustomData = Single.just(jsonHeadResponse);
        when(rRepo.getHeadConfig_FromDevice()).thenReturn(headResponseCustomData);

        float[] expected = new float[]{0.1293f, 0.0819f, 0.0568f, 0.0404f};
        calc.runCalc(circuit)
                .test()
                .assertNoErrors()
                .assertValue(actual -> Converters.equals(actual, expected, 0.01f))
                .dispose();
    }

    @Test
    public void calc_JsonEmpty_Incorrect() {
        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(Single.just(new JsonHeadResponse()));

        float[] expected = new float[]{0, 0, 0, 0};
        calc.runCalc(circuit)
                .test()
                .assertNoErrors()
                .assertValue(actual -> Arrays.equals(actual, expected))
                .dispose();
    }
}