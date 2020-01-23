package com.kolesova_violetta.ltc.ui.fragments.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.kolesova_violetta.ltc.sms.AlarmSetting;

public class MonitoringViewModel extends ViewModel {

    public void monitoringOff(Context context) {
        AlarmSetting.cancelAlarmRTC(context);
    }
}
