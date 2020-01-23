package com.kolesova_violetta.ltc.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.device_as_server.PreferenceChangeListenerForDevice;
import com.kolesova_violetta.ltc.datastore.device_as_server.try_connect.InternetAvailabilityChecker;
import com.kolesova_violetta.ltc.datastore.device_as_server.try_connect.InternetConnectivityListener;
import com.kolesova_violetta.ltc.handlers.TextFilters;
import com.kolesova_violetta.ltc.ui.custom.ConditionalEditTextPreference;
import com.kolesova_violetta.ltc.ui.dialogs.DialogWithCloseConditional;

public abstract class CustomPreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener, InternetConnectivityListener {

    final static EditTextPreference.OnBindEditTextListener sBindOnlyNumbers
            = editText -> {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.selectAll();
    };
    final static EditTextPreference.OnBindEditTextListener sBindOnlyLatin
            = editText -> {
        TextFilters.setEditTextFilterOnlyLatin(editText);
        editText.selectAll();
    };
    private InternetAvailabilityChecker mInternetAvailabilityChecker;
    private PreferenceChangeListenerForDevice mSavingChanges;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSavingChanges = new PreferenceChangeListenerForDevice(new Repository(getContext()));
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        mInternetAvailabilityChecker.addInternetConnectivityListener(this);

        PreferenceScreen screen = getPreferenceScreen();
        bindScreen(screen);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mInternetAvailabilityChecker.removeInternetConnectivityChangeListener(this);

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        mSavingChanges.run();
    }

    @Override
    public void onStop() {
        super.onStop();

        unbindScreen(getPreferenceScreen());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mSavingChanges.addChange(key);
    }

    protected void bindScreen(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = pGrp.getPreferenceCount() - 1; i >= 0; i--) {
                pGrp.getPreference(i).setIconSpaceReserved(false);
                bindScreen(pGrp.getPreference(i));
            }
        } else {
            // Удаление пространства под изображение во всех пунктах
            p.setIconSpaceReserved(false);
            bindPreference(p);
        }
    }

    protected void bindPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;

            listPreference.callChangeListener(listPreference.getValue());
        }

        if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;

            editTextPreference.callChangeListener(editTextPreference.getText());
        }
    }

    private void unbindScreen(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = pGrp.getPreferenceCount() - 1; i >= 0; i--) {
                unbindScreen(pGrp.getPreference(i));
            }
        } else {
            unbindPreference(p);
        }
    }

    protected void unbindPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(null);
        preference.setOnPreferenceClickListener(null);

        if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            editTextPreference.setOnBindEditTextListener(null);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (value != null) dependencyChanges(preference.getKey(), value.toString());
        return true;
    }

    protected void dependencyChanges(String key, String toString) {
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ConditionalEditTextPreference) {
            showDialog(DialogWithCloseConditional.newInstance(preference.getKey()));
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    void showDialog(DialogFragment dialog) {
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), dialog.getClass().getName());
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        getPreferenceScreen().setEnabled(isConnected);
    }
}
