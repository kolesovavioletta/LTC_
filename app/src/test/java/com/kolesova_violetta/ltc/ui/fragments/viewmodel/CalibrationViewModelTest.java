package com.kolesova_violetta.ltc.ui.fragments.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;
import com.kolesova_violetta.ltc.calculations.CalcAndSaveCalibrationData;
import com.kolesova_violetta.ltc.datastore.CustomData;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class CalibrationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Observer<Response<Void, Exception>> observerVoid;
    @Mock
    private Observer<String> observerStr;
    @Mock
    private CalcAndSaveCalibrationData calc;

    private CalibrationViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        viewModel = spy(
                new CalibrationViewModel(
                        mock(Repository.class), mock(SharedPreferencesRepository.class)));
        viewModel.getSensorErrorLiveData().observeForever(observerStr);
        doReturn(new String[]{"0"})
                .when(viewModel)
                .getWeightsTractor();
        doReturn(new String[]{"0"})
                .when(viewModel)
                .getWeightsTrailer();
        doReturn("Name")
                .when(viewModel)
                .getDriverNameAfterSuccessCalibration();
        doReturn("Time")
                .when(viewModel)
                .getDatetimeAfterSuccessCalibration();
        doReturn(calc)
                .when(viewModel)
                .makeCalcAndSaveCalibr(any(Repository.class), any(SharedPreferencesRepository.class));
    }

    @After
    public void tearDown() throws Exception {
        observerVoid = null;
        observerStr = null;
        viewModel = null;
    }

    private void setCalcResponse(CustomData<boolean[]> response) {
        when(calc.start()).thenReturn(response);

        viewModel.onEndInputWeights(any(), any()).observeForever(observerVoid);
    }

    @Test
    public void afterSuccessSave_DeviceCorrect_Correct() {
        setCalcResponse(CustomData.getInstance(new boolean[]{false, false}));

        verify(observerVoid).onChanged(Response.success(null));
    }

    @Test
    public void afterSuccessSave_TwiceDeviceInvalid_Correct() {
        setCalcResponse(CustomData.getInstance(new boolean[]{false, true}));

        verify(observerVoid).onChanged(Response.success(null));
        verify(observerStr).onChanged("2");
    }

    @Test
    public void afterSuccessSave_BothDeviceInvalid_Correct() {
        setCalcResponse(CustomData.getInstance(new boolean[]{true, true}));

        verify(observerVoid).onChanged(Response.success(null));
        verify(observerStr).onChanged("1, 2");
    }

    @Test
    public void afterFailSave_Correct() {
        VolleyError error = new VolleyError("something");
        setCalcResponse(CustomData.getInstance(error));

        Response<Void, Exception> expected = Response.error(error);

        verify(observerVoid).onChanged(expected);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void afterSuccessSave_NullArrAboutDevice_Incorrect() {
        setCalcResponse(CustomData.getInstance(null));

        verify(observerVoid).onChanged(Response.success(null));
        verify(observerStr).onChanged("");
    }

//    @Test
//    public void createErrSensorString() {
//        boolean[] arr1 = new boolean[]{true, true};
//        String s1 = viewModel.createErrSensorString(arr1);
//        Assert.assertEquals("1, 2", s1);
//
//        boolean[] arr2 = new boolean[]{false, false};
//        String s2 = viewModel.createErrSensorString(arr2);
//        Assert.assertEquals("", s2);
//
//        boolean[] arr3 = new boolean[]{false, true};
//        String s3 = viewModel.createErrSensorString(arr3);
//        Assert.assertEquals("2", s3);
//
//        boolean[] arr4 = new boolean[]{true, false};
//        String s4 = viewModel.createErrSensorString(arr4);
//        Assert.assertEquals("1", s4);
//    }
}