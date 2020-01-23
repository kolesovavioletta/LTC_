package com.kolesova_violetta.ltc.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kolesova_violetta.ltc.R;

public class InteruptCalibrationDialog extends DialogWithListenerForStandardButtons {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.calibration_not_completed)
                .setMessage(R.string.calibraion_not_completed_message)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setPositiveButton(R.string.continue_w, null)
                .setNegativeButton(R.string.exit, (dialog1, which) -> getListener().onDialogNegativeClick(this))
                .create();
    }
}
