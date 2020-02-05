package com.kolesova_violetta.ltc.ui.fragments.view;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;

import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.R;
import com.kolesova_violetta.ltc.handlers.BaseSchedulerProvider;
import com.kolesova_violetta.ltc.handlers.SchedulerProvider;
import com.kolesova_violetta.ltc.mock.SmsSender;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.datastore.device_as_server.try_connect.InternetConnectivityListener;
import com.kolesova_violetta.ltc.ui.UiHelper;
import com.kolesova_violetta.ltc.ui.activity.ShowingProgressDialogFromFragment;
import com.kolesova_violetta.ltc.ui.dialogs.CalibrationDialog;
import com.kolesova_violetta.ltc.ui.dialogs.CaptchaDialog;
import com.kolesova_violetta.ltc.ui.dialogs.DialogListenerForThreeStandardButtons;
import com.kolesova_violetta.ltc.ui.dialogs.InteruptCalibrationDialog;
import com.kolesova_violetta.ltc.ui.dialogs.SimpleDialog;
import com.kolesova_violetta.ltc.ui.dialogs.UnsafeChangesWarningDialog;
import com.kolesova_violetta.ltc.ui.fragments.viewmodel.CalibrationViewModel;

/**
 * Калибровка устройства. Для откалибровки необходимо, чтобы:
 * 1. Пользователь ввел массы осей без груза через диалоговые окна.
 * 2. Посчитались коэффициенты контуров и сохранились на устройство с другими сопутствующими данными.
 * После чего необходимо отображать актуальные данные в текущем фрагменте.
 * <p>
 * Отображение введенных масс осей реализуется для debug версии.
 */
public class CalibrationPreferenceFragment extends CustomPreferenceFragment
        implements DialogListenerForThreeStandardButtons, InternetConnectivityListener {
    public static final String PREF_WEIGHT_AXLE_UNDER_CAB = "tractor_axle_1_et";
    public static final String PREF_AXLE_TRACTOR = "tractor_axle_"; // + N + "_et"
    public static final String PREF_AXLE_TRAILER = "trailer_axle_"; // + N + "_et"
    public static final String PREF_CALIBRATION_DRIVER_NAME = "calibration_name_et";
    public static final String PREF_CALIBRATION_DATE = "calibration_date_et";
    private static final int PERMISSIONS_REQUEST = 102;

    private PreferenceCategory tractorCategory;
    private PreferenceCategory trailerCategory;
    private Preference startCalibration;
    private DialogFragment currentWeightDialog;
    /**
     * Запуск тарировки
     */
    private final Preference.OnPreferenceClickListener sBindShowAxesDialogs = preference -> {
                showCalibrationDialogPref();
                return false;
            };
    private CalibrationViewModel mViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set ViewModel
        Repository r1 = new Repository(getContext());
        SharedPreferencesRepository r2 = new SharedPreferencesRepository(getContext());
        BaseSchedulerProvider schedulerProvider = new SchedulerProvider();
        CalibrationViewModel.Factory vmFactory = new CalibrationViewModel.Factory(r1, r2, schedulerProvider);
        mViewModel = ViewModelProviders.of(this, vmFactory).get(CalibrationViewModel.class);

        mViewModel.getSensorErrorLiveData().observe(this, this::createErrSensorDialog);
        mViewModel.getCoefSaved().observe(this, success -> {
            if (success) {
                refreshScreen(getPreferenceScreen());
                sendSmsAfterCalibration();
            }
            createResultDialog(success);
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_calibration);
        setHasOptionsMenu(true);

        tractorCategory = findPreference("tractor_category");
        trailerCategory = findPreference("trailer_category");
        startCalibration = findPreference("calibration");
    }

    @Override
    public void onStart() {
        super.onStart();

        int sumAxesTractor = mViewModel.getAxesTractorCount();
        int sumAxesTrailer = mViewModel.getAxesTrailerCount();

        if (tractorCategory != null && trailerCategory != null) {
            showChildPreferenceOfCategory(tractorCategory, sumAxesTractor);
            showChildPreferenceOfCategory(trailerCategory, sumAxesTrailer);

            tractorCategory.setEnabled(false);
            trailerCategory.setEnabled(false);
        }

        if (startCalibration != null) {
            startCalibration.setOnPreferenceClickListener(sBindShowAxesDialogs);
        }

        SwitchPreference switchDetails = findPreference("calibration_switch");

        // Данные калибровки можно отображать только для дебага
        if (BuildConfig.DEBUG) {
            if (switchDetails != null) {
                switchDetails.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean flag = (boolean) newValue;
                    if (tractorCategory != null) tractorCategory.setVisible(flag);
                    if (trailerCategory != null) trailerCategory.setVisible(flag);
                    return true;
                });
                switchDetails.callChangeListener(switchDetails.isChecked());
            }
        } else {
            showDialogUnsafeChanges();

            switchDetails.setVisible(false);
            tractorCategory.setVisible(false);
            trailerCategory.setVisible(false);
        }
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        startCalibration.setEnabled(isConnected);
    }

    /**
     * Диалог, предупреждающий об опасности дальнейших изменений.
     */
    private void showDialogUnsafeChanges() {
        DialogFragment d = new UnsafeChangesWarningDialog();
        showDialog(d);
    }

    /**
     * Диалог для защиты от случайного нажатия.
     */
    private void showCaptchaDialog() {
        showDialog(new CaptchaDialog());
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof UnsafeChangesWarningDialog) {
            showCaptchaDialog();
        } else if (dialog instanceof CaptchaDialog) {
            CaptchaDialog captchaDialog = (CaptchaDialog) dialog;
            if (captchaDialog.isFailCaptcha()) showCaptchaDialog();
        } else if (dialog instanceof CalibrationDialog) {
            onFinishCalibration(dialog);
        }
    }

    private void onFinishCalibration(DialogFragment dialog) {
        ((ShowingProgressDialogFromFragment) getActivity()).showProgressDialog(true);
        CalibrationDialog calibrDialog = (CalibrationDialog) dialog;
        mViewModel.onEndInputWeights(
                calibrDialog.getTractorWeights(), calibrDialog.getTrailerWeights());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof UnsafeChangesWarningDialog || dialog instanceof CaptchaDialog) {
            Activity a = getActivity();
            if (a != null) {
                a.onBackPressed();
            }
        } else if (dialog instanceof InteruptCalibrationDialog) {
            currentWeightDialog.dismiss();
        } else if (dialog instanceof CalibrationDialog) {
            showDialog(new InteruptCalibrationDialog());
        }
    }

    @Override
    public void onDialogNeutralButton(DialogFragment dialog) {
    }

    /**
     * Вывод полей для демонстрации масс осей тягача или прицепа
     *
     * @param category Категория тягач или прицеп
     * @param count    количество осей, для которых устанавливаются массы
     */
    private void showChildPreferenceOfCategory(PreferenceCategory category, int count) {
        if (count == 0) {
            category.getParent().removePreference(category);
            return;
        }

        while (category.getPreferenceCount() != count) {
            category.removePreference(category.getPreference(count));
        }
    }

    @Override
    protected void bindPreference(Preference preference) {
        super.bindPreference(preference);

        if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).setOnBindEditTextListener(sBindOnlyNumbers);
        }
    }

    private void showCalibrationDialogPref() {
        String[] tractorCircuits = mViewModel.getWeightsTractor();
        String[] trailerCircuits = mViewModel.getWeightsTrailer();
        currentWeightDialog = new CalibrationDialog(tractorCircuits, trailerCircuits);
        showDialog(currentWeightDialog);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Activity a = getActivity();
            if (a != null) {
                a.onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createResultDialog(boolean success) {
        ((ShowingProgressDialogFromFragment) getActivity()).showProgressDialog(false);
        if (success) {
            showDialog(new SimpleDialog(android.R.string.ok, R.string.calibration_success_end));
        } else {
            showDialog(new SimpleDialog(R.string.error, R.string.notif_err_repeat_calibration));
        }
    }

    private void createErrSensorDialog(String sensorNumbers) {
        if (!sensorNumbers.isEmpty()) {
            DialogFragment d = new SimpleDialog(
                    getString(R.string.notif_err_sensor),
                    getString(R.string.notif_err_sensor_n, sensorNumbers));
            showDialog(d);
        }
    }

    private void refreshScreen(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                refreshScreen(pGrp.getPreference(i));
            }
        } else {
            refreshPreference(p);
        }
    }

    private void refreshPreference(Preference preference) {
        if(! (preference instanceof EditTextPreference)) {
            return;
        }
        String value = mViewModel.getPreferenceValue(preference.getKey(), "0");
        EditTextPreference editTextPreference = (EditTextPreference) preference;
        if(editTextPreference.callChangeListener(value)) {
            editTextPreference.setText(value);
        }
    }

    // ---------------- sendSmsAfterCalibration ----------------------------------------------------

    private void sendSmsAfterCalibration() {
        requestPermissionsSendSmsAndGetLocation();
    }

    private void requestPermissionsSendSmsAndGetLocation() {
        // Запросы на отправку СМС и получения геопозиции
        String[] perm = SmsSender.getNeededPermission();
        String[] requestPerm = UiHelper.selectNotGrantedPermissions(perm, getContext());

        if (requestPerm.length > 0) {
            // Запрос разрешений
            requestPermissions(requestPerm, PERMISSIONS_REQUEST);
        } else {
            mViewModel.sendSmsAfterCalibration(getContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) { // if send sms GRANTED
                    mViewModel.sendSmsAfterCalibration(getContext());
                }
                break;
            }
        }
    }

    // ---------------- [END] sendSmsAfterCalibration ----------------------------------------------
}
