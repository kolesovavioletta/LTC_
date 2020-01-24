package com.kolesova_violetta.ltc.calculations;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.calculations.CalcHeadCalibrCoefExecutor;
import com.kolesova_violetta.ltc.calculations.CalcTrailerCalibrCoefExecutor;
import com.kolesova_violetta.ltc.datastore.FailCallback;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SuccessCb;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.kolesova_violetta.ltc.handlers.LiveDataUtils.createLiveData;
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
        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(
                        "{\"Head-k1\":5000,\"Head-k2\":10000,\"Head-k3\":15000,\"Head-k4\":20000}")));

        calc.runCalc(circuit)
                .observeForever(observer);
        verify(observer).onChanged(new SuccessCb<>(new float[]{0.1293f, 0.0819f, 0.0568f, 0.0404f}));
    }

    @Test
    public void calc_JsonConvert_Correct() {
        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(
                        "{\"Error\":5000,\"Head-k2\":10000,\"Head-k3\":15000,\"Head-k4\":20000}")));

        calc.runCalc(circuit)
                .observeForever(observer);
        verify(observer).onChanged(new SuccessCb<>(new float[]{0, 0.0819f, 0.0568f, 0.0404f}));
    }

    @Test
    public void calc_JsonEmpty_Incorrect() {
        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>("")));

        calc.runCalc(circuit)
                .observeForever(observer);
        verify(observer).onChanged(new FailCallback<>(new NullPointerException()));
    }

    @Test
    public void calc_JsonNull_Incorrect() {
        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>()));

        calc.runCalc(circuit)
                .observeForever(observer);
        verify(observer).onChanged(new FailCallback<>(new NullPointerException()));
    }
}