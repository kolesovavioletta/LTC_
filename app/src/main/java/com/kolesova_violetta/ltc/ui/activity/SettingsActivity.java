package com.kolesova_violetta.ltc.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.kolesova_violetta.ltc.R;
import com.kolesova_violetta.ltc.ui.UiHelper;
import com.kolesova_violetta.ltc.ui.fragments.CalibrationPreferenceFragment;
import com.kolesova_violetta.ltc.ui.fragments.MonitoringPreferenceFragment;
import com.kolesova_violetta.ltc.ui.fragments.SwitchingFragmentsOfSettingAct;

public class SettingsActivity extends AppCompatActivity implements SwitchingFragmentsOfSettingAct,
       ShowingProgressDialogFromFragment {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.settings_activity);

        progressDialog = UiHelper.createWaitProgressDialog(this);

        openHeadersFragment();
    }

    /**
     * Set up the {@link ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
        progressDialog = null;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) finish();
        else super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 1) finish();
        else fm.popBackStack();
        return true;
    }

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openCircuitsFragment(final int mode) {
//        CircuitsPreferenceFragment frag = CircuitsPreferenceFragment.getInstance(mode);
//        openFragment(frag);
    }

    @Override
    public void openGeneralFragment() {
//        openFragment(new GeneralPreferenceFragment());
    }

    @Override
    public void openCalibrationFragment() {
        openFragment(new CalibrationPreferenceFragment());
    }

    @Override
    public void openMonitoringFragment() {
        openFragment(new MonitoringPreferenceFragment());
    }

    @Override
    public void openEngineeringFragment() {
//        openFragment(new EngineeringFragment());
    }

    @Override
    public void openAboutUsFragment() {
//        openFragment(new AboutUsFragment());
    }

    public void openHeadersFragment() {
        openFragment(new HeaderFragment());
    }

    @Override
    public void showProgressDialog(boolean b) {
        if (b) {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    //-------------------------- HeaderFragment ----------------------------------------------------

    public static class HeaderFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceClickListener {

        private SwitchingFragmentsOfSettingAct mCallback;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_headers, rootKey);
        }

        @Override
        public void onStart() {
            super.onStart();
            bind();
        }

        private void bind() {
            setListenerOnScreen(getPreferenceScreen(), this);
        }

        @Override
        public void onStop() {
            super.onStop();
            unbind();
        }

        private void unbind() {
            setListenerOnScreen(getPreferenceScreen(), null);
        }

        private void setListenerOnScreen(PreferenceScreen screen, Preference.OnPreferenceClickListener listener) {
            int count = screen.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference p = screen.getPreference(i);
                p.setOnPreferenceClickListener(listener);
            }
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            mCallback = (SwitchingFragmentsOfSettingAct) getActivity();
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mCallback = null;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "config":
                    mCallback.openGeneralFragment();
                    break;
                case "monitoring":
                    mCallback.openMonitoringFragment();
                    break;
                case "diagnostics":
                    mCallback.openEngineeringFragment();
                    break;
                case "about_us":
                    mCallback.openAboutUsFragment();
                    break;
            }
            return false;
        }
    }
}
