package com.kolesova_violetta.ltc.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;

public class DialogWithCloseConditional extends EditTextPreferenceDialogFragmentCompat {

    private Button positiveButton;
    private EditText editText;
    private ConditionalFactory.ConditionalOfCloseDialogPref conditional;

    public static DialogWithCloseConditional newInstance(String key) {
        final DialogWithCloseConditional fragment = new DialogWithCloseConditional();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog2 -> {                    //
            positiveButton = ((AlertDialog) dialog2)
                    .getButton(AlertDialog.BUTTON_POSITIVE);
            if(editText.getText().toString().equals("0")) {
                editText.setText("");
            }
        });

        conditional = ConditionalFactory.getConditionalByKey(getPreference().getKey());

        return dialog;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        editText = view.findViewById(android.R.id.edit);
        editText.removeTextChangedListener(m_watcher);
        editText.addTextChangedListener(m_watcher);
        onEditTextChanged();
    }

    private class EditTextWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onEditTextChanged();
        }
    }

    private EditTextWatcher m_watcher = new EditTextWatcher();

    private void onEditTextChanged() {
        if (positiveButton != null && conditional != null) {
            boolean enable = conditional.isSuccess(editText.getText().toString());
            positiveButton.setEnabled(enable);
        }
    }
}
