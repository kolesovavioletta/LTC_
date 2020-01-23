package com.kolesova_violetta.ltc.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.kolesova_violetta.ltc.R;
import com.kolesova_violetta.ltc.handlers.TimeHelper;
import com.kolesova_violetta.ltc.sms.SmsSender;
import com.kolesova_violetta.ltc.ui.UiHelper;
import com.kolesova_violetta.ltc.ui.dialogs.TimeDialog;
import com.kolesova_violetta.ltc.ui.fragments.viewmodel.MonitoringViewModel;

import java.util.Calendar;
import java.util.TimeZone;

import ru.tinkoff.decoro.slots.PredefinedSlots;

import static android.content.Context.POWER_SERVICE;
import static com.kolesova_violetta.ltc.handlers.InputMaskHandler.setMaskOnEditText;

public class MonitoringPreferenceFragment extends CustomPreferenceFragment {
    public static final String PREF_MONITORING = "is_monitoring_event";
    public static final String PREF_PHONE_1 = "monitoring_phone_1_et";
    public static final String PREF_SMS_FREQUENCY = "monitoring_frequency_sms_list";
    public static final String PREF_TIME_SMS = "monitoring_time_sms_et";
    static final String PREF_MONITORING_CATEGORY = "category_monitoring";

    private final static EditTextPreference.OnBindEditTextListener sBindOnlyNumbersForPhone
            = editText -> {
        setMaskOnEditText(PredefinedSlots.RUS_PHONE_NUMBER, editText);
        editText.setInputType(editText.getInputType() | InputType.TYPE_CLASS_NUMBER);
    };

    private static final int PERMISSIONS_REQUEST = 102;
    private SwitchPreference mSwitchMonitoring;

    private MonitoringViewModel mViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(MonitoringViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_monitoring);
        setHasOptionsMenu(true);

        mSwitchMonitoring = findPreference(PREF_MONITORING);
    }

    @Override
    protected void bindPreference(Preference preference) {
        super.bindPreference(preference);

        switch (preference.getKey()) {
            case PREF_PHONE_1:
                ((EditTextPreference) preference).setOnBindEditTextListener(sBindOnlyNumbersForPhone);
                break;
        }

    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference.getKey().equals(PREF_TIME_SMS)) {
            showTimePreferenceDialog((EditTextPreference) preference);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    /*
     * Показывается диалог для установки времени. Результат выбора сохраняется. (и отображается в summary)
     * @param preference
     */
    private void showTimePreferenceDialog(EditTextPreference preference) {
        // получение введенного ранее времени
        String curTime = preference.getSummary().toString();
        int lastHours = TimeHelper.parseHour(curTime);
        int lastMinutes = TimeHelper.parseMinute(curTime);
        // установка этого времени в календарь без учета часового пояса
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(TimeHelper.getDateByHoursAndMinutes(lastHours, lastMinutes));
        // запуск диалога для смены времени
        TimeDialog timeDialog = new TimeDialog(calendar, (timePicker, h, m) -> {
            //сохранение результатов в память и в summary
            String time = TimeHelper.timeToString(h, m);
            if (preference.callChangeListener(time)) {
                preference.setText(time);
            }
        });
        showDialog(timeDialog);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference.getKey().equals(PREF_MONITORING)) {
            super.onPreferenceChange(preference, value);
            if ((boolean) value) return requestPermissionsSendSmsAndGetLocation();
            else {
                mViewModel.monitoringOff(getContext());
                return true;
            }
        } else return super.onPreferenceChange(preference, value);
    }

    private boolean requestPermissionsSendSmsAndGetLocation() {
        // Запрос игнорирования ограничений в фоне
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getContext().getPackageName();
            PowerManager pm = (PowerManager) getContext().getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

        // Запросы на отправку СМС и получения геопозиции
        String[] perm = SmsSender.getNeededPermission();
        String[] requestPerm = UiHelper.selectNotGrantedPermissions(perm, getContext());

        if (requestPerm.length > 0) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                // Пояснение
                Toast.makeText(getContext(), R.string.notif_info_no_permission_sms, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Запрос разрешений
                requestPermissions(requestPerm, PERMISSIONS_REQUEST);
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    mSwitchMonitoring.setChecked(false);
                }
                break;
            }
        }
    }
}
