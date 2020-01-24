package com.kolesova_violetta.ltc.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

/**
 * Preference с автоматическим обновлением значения в Summary, а так же этот класс используется для
 * условного диалогового окна (разрешает запись нового значения только если данные отвеают условию)
 */
public class ConditionalEditTextPreference extends EditTextPreference {
    public ConditionalEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ConditionalEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ConditionalEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConditionalEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        if(super.getSummary() == null) return getText();

        String summary = super.getSummary().toString();
        return String.format(summary, getText());
    }
}
