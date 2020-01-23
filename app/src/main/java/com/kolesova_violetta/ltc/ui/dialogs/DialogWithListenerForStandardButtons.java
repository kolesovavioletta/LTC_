package com.kolesova_violetta.ltc.ui.dialogs;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.kolesova_violetta.ltc.R;

public class DialogWithListenerForStandardButtons extends DialogFragment {

    private DialogListenerForThreeStandardButtons mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            //mListener = (DialogListenerForThreeStandardButtons) context;
            mListener = (DialogListenerForThreeStandardButtons) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DialogListenerForThreeStandardButtons");
        }
    }

    DialogListenerForThreeStandardButtons getListener() {
        return mListener;
    }

    static FrameLayout createViewWithText(Context context, String text) {
        final EditText editText = new EditText(context);
        editText.setId(R.id.edit_text_field);
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        editText.setLayoutParams(params);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setText(text);
        editText.setOnFocusChangeListener((v, hasFocus) -> editText.post(() -> {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }));
        editText.requestFocus();
        editText.selectAll();
        container.addView(editText);
        return container;
    }
}
