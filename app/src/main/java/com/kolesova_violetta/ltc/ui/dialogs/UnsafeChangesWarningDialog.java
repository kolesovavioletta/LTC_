package com.kolesova_violetta.ltc.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kolesova_violetta.ltc.R;

public class UnsafeChangesWarningDialog extends DialogWithListenerForStandardButtons {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.unsafe_change)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setMessage(getString(R.string.dangerous_warning) +
                        "\n\n" + getString(R.string.err_no_conection_in_calibration) +
                        "\n\n" + getString(R.string.note_send_sms))
                .setPositiveButton(R.string.continue_w, (dialog, id) -> getListener().onDialogPositiveClick(this))
                .setNegativeButton(R.string.dialog_button_break_w, (dialog, id) -> getListener().onDialogNegativeClick(this));

        return builder.create();
    }
}
