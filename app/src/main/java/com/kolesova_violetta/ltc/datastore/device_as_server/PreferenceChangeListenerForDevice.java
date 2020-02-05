package com.kolesova_violetta.ltc.datastore.device_as_server;

import com.kolesova_violetta.ltc.mock.AlarmSetting;
import com.kolesova_violetta.ltc.BuildConfig;
import com.kolesova_violetta.ltc.mock.MonitoringCache;
import com.kolesova_violetta.ltc.datastore.Repository;

import java.util.HashSet;
import java.util.Set;

import static com.kolesova_violetta.ltc.mock.Const.CIRCUITS_COUNT;
import static com.kolesova_violetta.ltc.mock.Const.PREF_DRIVER_NAME;
import static com.kolesova_violetta.ltc.ui.fragments.view.MonitoringPreferenceFragment.PREF_PHONE_1;

public class PreferenceChangeListenerForDevice {

    private Repository mRepo;

    public PreferenceChangeListenerForDevice(Repository repo) {
        mRepo = repo;
    }

    private enum TypeData {
        TRACTOR_CONSTANT,
        DRIVER,
        TRAILER_CONSTANT,
        SMS, PHONE
    }

    private Set<TypeData> mChanges = new HashSet<>();

    public void addCommonChanges() {
        mChanges.add(TypeData.DRIVER);
        mChanges.add(TypeData.TRACTOR_CONSTANT);
        mChanges.add(TypeData.TRAILER_CONSTANT);
    }

    public void addChange(String key) {
        if (key.equals(PREF_DRIVER_NAME)) {
            mChanges.add(TypeData.DRIVER);
        } else if (key.contains("tractor") || isTractorCircuitChange(key)) {
            mChanges.add(TypeData.TRACTOR_CONSTANT);
            mChanges.add(TypeData.TRAILER_CONSTANT);
        } else if (key.contains("trailer") || isTrailerCircuitChange(key)) {
            mChanges.add(TypeData.TRACTOR_CONSTANT);
            mChanges.add(TypeData.TRAILER_CONSTANT);
        } else if (key.equals(PREF_PHONE_1)) {
            mChanges.add(TypeData.PHONE);
        } else if (key.contains("monitoring")) {
            mChanges.add(TypeData.SMS);
        }
    }

    private boolean isTractorCircuitChange(String key) {
        if (!key.contains("circuit_")) return false;

        int index = Character.getNumericValue(key.charAt(key.length() - 1));
        return index > 0 && index <= CIRCUITS_COUNT;
    }

    private boolean isTrailerCircuitChange(String key) {
        if (!key.contains("circuit_")) return false;

        int index = Character.getNumericValue(key.charAt(key.length() - 1));
        return index > CIRCUITS_COUNT && index < 9;
    }

    public void run() {
        if (mChanges.contains(TypeData.TRACTOR_CONSTANT)) mChanges.remove(TypeData.DRIVER);

        String url = null;
        for (TypeData type : mChanges) {
            switch (type) {
                case DRIVER:
                    url = getRequestDriverName();
                    break;
                case TRACTOR_CONSTANT:
                    url = saveOnDeviceTractorConstant();
                    break;
                case TRAILER_CONSTANT:
                    url = saveOnDeviceTrailerConstant();
                    break;
                case PHONE:
                    url = DeviceQueries.createUrlForPhone(MonitoringCache.getPhoneNumber());
                    // Без break! необходимо переустановить расписание уведомления и СМС, если режим мониторинга включен
                case SMS:
                    if(MonitoringCache.isNeedSend()) {
                        setAlarmForSms();
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
            if (url != null) {
                mRepo.setRequest(url);
            }
        }
    }

    public void setAlarmForSms() {
        if (!MonitoringCache.isNeedSend()) return;

        //...

        if(BuildConfig.DEBUG) {
            AlarmSetting.scheduleNotification(/*...*/);
        } else {
            AlarmSetting.scheduleNotification(/*...*/);
        }
    }

    private String getRequestDriverName() { return null; }

    private String saveOnDeviceTractorConstant() {
        return null;
    }

    private String saveOnDeviceTrailerConstant() {
        return null;
    }
}
