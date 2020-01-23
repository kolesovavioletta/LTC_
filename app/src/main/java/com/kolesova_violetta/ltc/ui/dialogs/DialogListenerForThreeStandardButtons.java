package com.kolesova_violetta.ltc.ui.dialogs;

import androidx.fragment.app.DialogFragment;

public interface DialogListenerForThreeStandardButtons {
    void onDialogPositiveClick(DialogFragment dialog);
    void onDialogNegativeClick(DialogFragment dialog);
    void onDialogNeutralButton(DialogFragment dialog);
}
