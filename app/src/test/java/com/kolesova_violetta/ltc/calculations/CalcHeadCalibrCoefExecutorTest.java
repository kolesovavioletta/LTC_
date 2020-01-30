package com.kolesova_violetta.ltc.calculations;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.CustomData;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonHeadResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты ориентированы на оба класса {@link CalcHeadCalibrCoefExecutor}{@link CalcTrailerCalibrCoefExecutor}
 */
public class CalcHeadCalibrCoefExecutorTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Observer<Response<float[], Exception>> observer;
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
        circuit.add(new Circuit(2, "1", "A3","A4", 1400));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void calc_Correct() {
        JsonHeadResponse jsonHeadResponse = new JsonHeadResponse();
        jsonHeadResponse.setAllHeadAcd("5000", "10000", "15000", "20000");
        CustomData<JsonHeadResponse> headResponseCustomData = CustomData.getInstance(jsonHeadResponse);
        when(rRepo.getHeadConfig_FromDevice()).thenReturn(headResponseCustomData);

        calc.runCalc(circuit)
                .observeForever(observer);
        verify(observer).onChanged(Response.success(new float[]{0.1293f, 0.0819f, 0.0568f, 0.0404f}));
    }

    @Test
    public void calc_JsonEmpty_Incorrect() {
        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(CustomData.getInstance(new JsonHeadResponse()));

        calc.runCalc(circuit)
                .observeForever(observer);
        verify(observer).onChanged(Response.success(new float[]{0, 0, 0, 0}));
    }

    @Test
    public void calc_JsonNull_Incorrect() {
        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(CustomData.getInstance(null));

        calc.runCalc(circuit)
                .observeForever(observer);
        verify(observer).onChanged(Response.error(new NullPointerException()));
    }
}