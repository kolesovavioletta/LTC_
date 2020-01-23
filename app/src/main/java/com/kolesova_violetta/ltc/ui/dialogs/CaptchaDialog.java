package com.kolesova_violetta.ltc.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kolesova_violetta.ltc.R;

import static com.kolesova_violetta.ltc.handlers.Converters.getRand;

public class CaptchaDialog extends DialogWithListenerForStandardButtons {

    private FrameLayout layoutWithEditText;
    private int valueOfCaptcha;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        layoutWithEditText = createViewWithText(getContext(), null);
        valueOfCaptcha = getRand(50000, 59999);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.CAPTCHA)
                .setIcon(R.mipmap.icon)
                .setMessage(getString(R.string.enter_number) + " " + valueOfCaptcha)
                .setCancelable(false)
                .setView(layoutWithEditText)
                .setPositiveButton(R.string.continue_w, (dialog, id) -> getListener().onDialogPositiveClick(this))
                .setNegativeButton(R.string.dialog_button_break_w, (dialog, id) -> getListener().onDialogNegativeClick(this));

        return builder.create();
    }

    public boolean isFailCaptcha() {
        EditText et = (EditText) layoutWithEditText.getChildAt(0);
        String v = et.getText().toString();
        return (v.isEmpty() || Integer.valueOf(v) != valueOfCaptcha);
    }
}
