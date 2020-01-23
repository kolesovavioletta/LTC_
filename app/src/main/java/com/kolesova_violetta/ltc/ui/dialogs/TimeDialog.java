package com.kolesova_violetta.ltc.ui.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * Диалог для установки времени
 */
public class TimeDialog extends DialogFragment {

    private Calendar dateAndTime;
    private TimePickerDialog.OnTimeSetListener callback;

    public TimeDialog(Calendar dateAndTime, TimePickerDialog.OnTimeSetListener callback) {
        this.dateAndTime = dateAndTime;
        this.callback = callback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        callback = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new TimePickerDialog(getContext(), callback,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true);
    }
}
