package com.kolesova_violetta.ltc.model.calculations;

import com.kolesova_violetta.ltc.model.Circuit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalcCalibrCoefExecutorTest {

    private CalcCalibrCoefExecutor calc;
    private List<Circuit> circuits;
    private int[] ACD = new int[]{100, 200, 300, 400};
    private float[] floatZeroArr = new float[]{0, 0, 0, 0};

    @Before
    public void setUp() throws Exception {
        calc = Mockito.mock(CalcCalibrCoefExecutor.class, Mockito.CALLS_REAL_METHODS);
        circuits = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {
        calc = null;
        circuits = null;
    }

    @Test
    public void calcCoefficients_minlSample_Correct() {
        circuits.add(new Circuit(2, "2", "A1", 300));

        float[] actual = calc.calcCoefficients(circuits, 10, ACD);
        float[] expected = new float[]{3.333f, 0, 0, 0};
        Assert.assertArrayEquals(expected, actual, 0.001f);
    }

    @Test
    public void calcCoefficients_maxSample_Correct() {
        circuits.add(new Circuit(1, "2", "A1", 600));
        circuits.add(new Circuit(2, "1", "A2", "A4", 1200));
        circuits.add(new Circuit(4, "2", "A3", 400));

        float[] actual = calc.calcCoefficients(circuits, 20, ACD);
        float[] expected = new float[]{7.5f, 3.333f, 1.579f, 1.429f};
        Assert.assertArrayEquals(expected, actual, 0.001f);
    }

    @Test
    public void calcCoefficients_Acd_Incorrect() {
        circuits.add(new Circuit(1, "2", "A1", 600));
        circuits.add(new Circuit(2, "1", "A2", "A4", 1200));
        circuits.add(new Circuit(4, "2", "A3", 400));

        float[] actual = calc.calcCoefficients(circuits, 20, new int[]{10, 200, 300, 400});
        float[] expected = new float[]{0f, 3.333f, 1.579f, 1.429f};
        Assert.assertArrayEquals(expected, actual, 0.001f);
    }

    @Test
    public void calcCoefficients_Var_Incorrect() {
        circuits.add(new Circuit(1, "2", "A1", 600));
        circuits.add(new Circuit(2, "1", "A2", "A", 1200));
        circuits.add(new Circuit(4, "2", "A", 400));

        float[] actual = calc.calcCoefficients(circuits, 20, ACD);
        float[] expected = new float[]{7.5f, 3.333f, 0f, 0f};
        Assert.assertArrayEquals(expected, actual, 0.001f);
    }

    @Test
    public void calcCoefficients_U0_Incorrect() {
        circuits.add(new Circuit(2, "2", "A1", 300));

        float[] actual = calc.calcCoefficients(circuits, 0, ACD);
        float[] expected = new float[]{3f, 0, 0, 0};
        Assert.assertArrayEquals(expected, actual, 0f);
    }

    @Test
    public void calcCoefficients_AcdSize_Incorrect() {
        circuits.add(new Circuit(2, "2", "A1", 300));
        circuits.add(new Circuit(2, "2", "A3", 800));

        float[] actual = calc.calcCoefficients(circuits, 0, new int[]{1});
        float[] expected = new float[]{300f, 0, 0, 0};
        Assert.assertArrayEquals(expected, actual, 0f);
    }

    @Test
    public void calcCoefficients_AcdEmpty_Incorrect() {
        circuits.add(new Circuit(2, "2", "A1", 300));
        circuits.add(new Circuit(2, "2", "A3", 800));

        float[] actual = calc.calcCoefficients(circuits, 20, new int[]{});
        Assert.assertArrayEquals(floatZeroArr, actual, 0f);
    }

    @Test
    public void calcCoefficients_AcdNull_Incorrect() {
        circuits.add(new Circuit(2, "2", "A1", 300));
        circuits.add(new Circuit(2, "2", "A3", 800));

        float[] actual = calc.calcCoefficients(circuits, 20, null);
        Assert.assertArrayEquals(floatZeroArr, actual, 0f);
    }

    @Test
    public void calcCoefficients_U0Empty_Incorrect() {
        float[] actual = calc.calcCoefficients(Collections.EMPTY_LIST, 20, ACD);
        Assert.assertArrayEquals(floatZeroArr, actual, 0f);
    }
}