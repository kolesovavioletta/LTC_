package com.kolesova_violetta.ltc.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kolesova_violetta.ltc.R;

public class SimpleDialog extends DialogFragment {

    private String sTitle;
    private int iTitle;
    private String sMessage;
    private int iMessage;

    public SimpleDialog(String title, String message) {
        this.sMessage = message;
        this.sTitle = title;
    }

    public SimpleDialog(@StringRes int title,@StringRes int message) {
        this.iMessage = message;
        this.iTitle = title;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(iMessage != 0) sMessage = getString(iMessage);
        if(iTitle != 0) sTitle = getString(iTitle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(sTitle)
                .setMessage(sMessage)
                .setIcon(R.mipmap.icon)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.dismiss());
        return builder.create();
    }
}
