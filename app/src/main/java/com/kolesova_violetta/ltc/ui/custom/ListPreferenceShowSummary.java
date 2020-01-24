package com.kolesova_violetta.ltc.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

/**
 * Preference с автоматическим обновлением значения в Summary
 */
public class ListPreferenceShowSummary extends ListPreference {
    public ListPreferenceShowSummary(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ListPreferenceShowSummary(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListPreferenceShowSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPreferenceShowSummary(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        if(super.getSummary() == null) return getEntry();

        String summary = super.getSummary().toString();
        return String.format(summary, getEntry());
    }
}
