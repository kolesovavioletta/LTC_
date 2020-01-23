package com.kolesova_violetta.ltc.calculations;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;
import com.kolesova_violetta.ltc.Circuit;
import com.kolesova_violetta.ltc.datastore.FailCallback;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.Response;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.datastore.SuccessCb;

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
import java.util.List;

import static com.kolesova_violetta.ltc.handlers.LiveDataUtils.createLiveData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тестирование класса {@link CalcAndSaveCalibrationData}. Акценты:
 *  - 4 типа установки + с прицепом или без
 *  - одностор. / двустор. контура
 *  - прогрешность массы оси (50кг)
 *  - прохождение теста с N попытки (N > 1 && N < 20)
 *  - датчики сломаны (получаемый массив в ответе)
 */
@Config(maxSdk = 28, minSdk = 28)
@RunWith(RobolectricTestRunner.class)
public class CalcAndSaveCalibrationDataTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Observer<Response<boolean[], Throwable>> observer;
    @Mock
    private Repository rRepo;
    @Mock
    private SharedPreferencesRepository lRepo;

    private CalcAndSaveCalibrationData calc;
    private LiveData<Response<boolean[], Throwable>> end;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        calc = new CalcAndSaveCalibrationData(rRepo, lRepo);
        when(lRepo.getDriverName()).thenReturn("DriverName");
        when(lRepo.getSteeringAxleWeight()).thenReturn(150);

        when(rRepo.getHeadConfig_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(
                        "{\"Head-k1\":5000,\"Head-k2\":10000,\"Head-k3\":15000,\"Head-k4\":20000}")));

        when(rRepo.getTrailerConfig_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(
                        "{\"Trailer-k1\":5000,\"Trailer-k2\":15000,\"Trailer-k3\":25000,\"Trailer-k4\":35000}"
                )));
    }

    @After
    public void tearDown() throws Exception {
        end.removeObserver(observer);
        end = null;
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
        when(rRepo.getWeights_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(weights)));

        end = calc.start();
        end.observeForever(observer);
        verify(observer).onChanged(new SuccessCb<>(new boolean[]{false, false}));
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
        when(rRepo.getWeights_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(weights)));

        end = calc.start();
        end.observeForever(observer);
        verify(observer).onChanged(new SuccessCb<>(new boolean[]{false, false}));
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
        when(rRepo.getWeights_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(weights)));

        end = calc.start();
        end.observeForever(observer);
        verify(observer).onChanged(
                new FailCallback<>(new IndexOutOfBoundsException(
                        "the limit of attempts to write data to the device has been exhausted")));
    }

    // тип установки - 2 блока. По 1 двустор. на тягаче и на прицепе
    private void installType2Single() {
        when(lRepo.getModeInstallation()).thenReturn(2);
        //--- тягач
        List<Circuit> circuitH = new ArrayList<>();
        circuitH.add(new Circuit(2, "2", "A1", 300));
        installHead(circuitH, (SuccessCb<Void, VolleyError>) SuccessCb.EMPTY);
        //--- прицеп
        when(lRepo.isExistTrailer()).thenReturn(true);

        List<Circuit> circuitT = new ArrayList<>();
        circuitT.add(new Circuit(3, "2", "B1", 800));

        installTrailer(circuitT, (SuccessCb<Void, VolleyError>) SuccessCb.EMPTY);
    }

    private void installHead(List<Circuit> circuits, Response<Void,VolleyError> onSave) {
        when(lRepo.getCircuitsTractor()).thenReturn(circuits);

        when(rRepo.setTractorCalibration_OnDevice(any(), anyInt(), anyString()))
                .thenReturn(createLiveData(onSave));
    }

    private void installTrailer(List<Circuit> circuits, Response<Void,VolleyError> onSave) {
        when(lRepo.getCircuitsTrailer()).thenReturn(circuits);

        when(rRepo.setTrailerCalibration_OnDevice(any(), anyString()))
                .thenReturn(createLiveData(onSave));
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
        when(rRepo.getWeights_FromDevice())
                .thenReturn(createLiveData(new SuccessCb<>(weights)));

        end = calc.start();
        end.observeForever(observer);
        verify(observer).onChanged(new SuccessCb<>(new boolean[]{false, false}));
    }

    // тип установки - 2 блока. Тягач: 1 двуст. + 1 пара одностор.; Прицеп: 1 пара одностор. + 1 двуст.
    private void installType2Mixed() {
        when(lRepo.getModeInstallation()).thenReturn(2);
        //--- тягач
        List<Circuit> circuitH = new ArrayList<>();
        circuitH.add(new Circuit(2, "2", "A1", 300));
        circuitH.add(new Circuit(1, "1", "A3","A4", 600));
        installHead(circuitH, (SuccessCb<Void, VolleyError>) SuccessCb.EMPTY);
        //--- прицеп
        when(lRepo.isExistTrailer()).thenReturn(true);

        List<Circuit> circuitT = new ArrayList<>();
        circuitT.add(new Circuit(2, "1", "B1","B3", 200));
        circuitT.add(new Circuit(1, "2", "B2", 600));
        installTrailer(circuitT, (SuccessCb<Void, VolleyError>) SuccessCb.EMPTY);
    }
}