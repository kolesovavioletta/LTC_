package com.kolesova_violetta.ltc.model.calculations;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonHeadResponse;
import com.kolesova_violetta.ltc.datastore.device_as_server.response.JsonTrailerResponse;
import com.kolesova_violetta.ltc.exception.DeviceException;
import com.kolesova_violetta.ltc.model.Circuit;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Config(maxSdk = 28, minSdk = 28)
@RunWith(RobolectricTestRunner.class)
public class CalcAndSaveCalibrationDataTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Repository rRepo;
    @Mock
    private SharedPreferencesRepository lRepo;

    private TestObserver<boolean[]> observer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(lRepo.getDriverName()).thenReturn("DriverName");
        when(lRepo.getSteeringAxleWeight()).thenReturn(150);

        JsonHeadResponse jsonHeadResponse = new JsonHeadResponse();
        jsonHeadResponse.setAllHeadAcd("5000", "10000", "15000", "20000");
        Single<JsonHeadResponse> headResponseCustomData = Single.just(jsonHeadResponse);
        when(rRepo.getHeadConfig_FromDevice()).thenReturn(headResponseCustomData);

        JsonTrailerResponse jsonTrailerResponse = new JsonTrailerResponse("5000", "15000", "25000", "35000");
        Single<JsonTrailerResponse> trailerResponseCustomData = Single.just(jsonTrailerResponse);
        when(rRepo.getTrailerConfig_FromDevice()).thenReturn(trailerResponseCustomData);
    }

    @After
    public void tearDown() throws Exception {
        observer.dispose();
        observer = null;
    }

    @Test
    public void start_Type2Single_Correct() {
        installType2Single();

        JSONObject weights = new JSONObject();
        try {
            weights.put("A1", 300);
            weights.put("B1", 800);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setWeights(weights);
        startCalc();

        observer
                .assertNoErrors()
                .assertValue(arr -> Arrays.equals(arr, new boolean[]{false, false}));
    }

    private void setWeights(JSONObject weights) {
        when(rRepo.getWeights_FromDevice_Json()).thenReturn(Single.just(weights));
    }

    private void startCalc() {
        observer = new CalcAndSaveCalibrationData(rRepo, lRepo)
                .start()
                .test();
    }

    @Test
    public void start_Type2Single_Delta_Correct() {
        installType2Single();

        JSONObject weights = new JSONObject();
        try {
            weights.put("A1", 400); // 300 + 2 * 50
            weights.put("B1", 650); // 800 - 3 * 50
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setWeights(weights);
        startCalc();

        observer
                .assertNoErrors()
                .assertValue(arr -> Arrays.equals(arr, new boolean[]{false, false}));
    }

    @Test
    public void start_Type2Single_Delta_Incorrect() {
        installType2Single();

        JSONObject weights = new JSONObject();
        try {
            weights.put("A1", 401); // err
            weights.put("B1", 800);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setWeights(weights);
        startCalc();

        observer
                .assertNoValues()
                .assertError(DeviceException.class);
    }

    // тип установки - 2 блока. По 1 двустор. на тягаче и на прицепе
    private void installType2Single() {
        when(lRepo.getModeInstallation()).thenReturn(2);
        //--- тягач
        List<Circuit> circuitH = new ArrayList<>();
        circuitH.add(new Circuit(2, "2", "A1", 300));
        installHead(circuitH, Completable.complete());
        //--- прицеп
        when(lRepo.isExistTrailer()).thenReturn(true);

        List<Circuit> circuitT = new ArrayList<>();
        circuitT.add(new Circuit(3, "2", "B1", 800));

        installTrailer(circuitT, Completable.complete());
    }

    private void installHead(List<Circuit> circuits, Completable onSave) {
        when(lRepo.getCircuitsTractor()).thenReturn(circuits);

        when(rRepo.setTractorCalibration_OnDevice(any(), anyInt(), anyString()))
                .thenReturn(onSave);
    }

    private void installTrailer(List<Circuit> circuits, Completable onSave) {
        when(lRepo.getCircuitsTrailer()).thenReturn(circuits);

        when(rRepo.setTrailerCalibration_OnDevice(any(), anyString()))
                .thenReturn(onSave);
    }

    @Test
    public void start_Type2Mixed_Correct() {
        installType2Mixed();

        JSONObject weights = new JSONObject();
        try {
            weights.put("A1", 300);
            weights.put("A3", 600);
            weights.put("A4", 400);
            weights.put("B1", 200);
            weights.put("B3", 800);
            weights.put("B2", 600);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setWeights(weights);
        startCalc();

        observer
                .assertNoErrors()
                .assertValue(arr -> Arrays.equals(arr, new boolean[]{false, false}));
    }

    // тип установки - 2 блока. Тягач: 1 двуст. + 1 пара одностор.; Прицеп: 1 пара одностор. + 1 двуст.
    private void installType2Mixed() {
        when(lRepo.getModeInstallation()).thenReturn(2);
        //--- тягач
        List<Circuit> circuitH = new ArrayList<>();
        circuitH.add(new Circuit(2, "2", "A1", 300));
        circuitH.add(new Circuit(1, "1", "A3", "A4", 600));
        installHead(circuitH, Completable.complete());
        //--- прицеп
        when(lRepo.isExistTrailer()).thenReturn(true);

        List<Circuit> circuitT = new ArrayList<>();
        circuitT.add(new Circuit(2, "1", "B1", "B3", 200));
        circuitT.add(new Circuit(1, "2", "B2", 600));
        installTrailer(circuitT, Completable.complete());
    }
}