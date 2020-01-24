package com.kolesova_violetta.ltc.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kolesova_violetta.ltc.R;
import com.kolesova_violetta.ltc.datastore.Repository;
import com.kolesova_violetta.ltc.datastore.SharedPreferencesRepository;
import com.kolesova_violetta.ltc.handlers.TextFilters;
import com.kolesova_violetta.ltc.ui.activity.ShowingProgressDialogFromFragment;
import com.kolesova_violetta.ltc.ui.fragments.viewmodel.RegistrationViewModel;

/**
 * Регистрация водителя. Водитель должен ввести ФИО в формате "ФамилияИО"/"ФамилияИ".
 * ФИО сохраняется на телефоне и на датчик.
 */
public class RegistrationFragment extends Fragment implements View.OnClickListener {

    private EditText mFullNameEditText;
    private Button mOkButton;

    private RegistrationViewModel mViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Repository r1 = new Repository(getContext());
        SharedPreferencesRepository r2 = new SharedPreferencesRepository(getContext());
        RegistrationViewModel.Factory vmFactory = new RegistrationViewModel.Factory(r1, r2);
        mViewModel = ViewModelProviders.of(this, vmFactory).get(RegistrationViewModel.class);

        mViewModel.getProgressVisibilityLiveData().observe(this, visible ->
                ((ShowingProgressDialogFromFragment) getActivity()).showProgressDialog(visible));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_registration, container, false);
        mFullNameEditText = root.findViewById(R.id.fio);
        mOkButton = root.findViewById(R.id.registration_b);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mOkButton.setOnClickListener(this);
        TextFilters.setEditTextFilterOnlyLatin(mFullNameEditText);
    }

    @Override
    public void onStop() {
        super.onStop();
        mOkButton.setOnClickListener(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registration_b:
                runRegistration();
                break;
        }
    }

    private void runRegistration() {
        mFullNameEditText.setError(null);
        String name = getFullName();
        if (mViewModel.isCorrectName(name)) {
            mViewModel.execRegistration(name).observe(this, response -> {
                if (response.isSuccess()) {
                    ExitFromFragment activityAction = (ExitFromFragment) getActivity();
                    activityAction.exitFromFragment(ExitFromFragment.EXIT_REGISTRATION);
                } else {
                    Toast.makeText(getContext(),
                            R.string.notif_error_name_not_saved, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mFullNameEditText.setError(getString(R.string.notif_error_wrong_data));
        }
    }

    private String getFullName() {
        return mFullNameEditText.getText().toString();
    }
}
