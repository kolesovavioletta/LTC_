package com.kolesova_violetta.ltc.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kolesova_violetta.ltc.PartOfCar;
import com.kolesova_violetta.ltc.R;

/**
 * Для всех осей тягача и прицепа поочередно в диалоге выводятся массы,
 * введенные при предыдущей калибровке, и изменяются пользователем.
 * По окончании калибровки введенные данные могут быть получены {@link #getTractorWeights()}
 * {@link #getTrailerWeights()}
 */
public class CalibrationDialog extends DialogWithListenerForStandardButtons
        implements DialogListenerForThreeStandardButtons {
    // Тягач
    private String TRACTOR_TITLE; // заголовок диалога
    private int mAxesTractorCount; // количество осей
    private String[] mWeightsTractor; // массы осей
    //Прицеп
    private String TRAILER_TITLE;
    private String[] mWeightsTrailer;

    private int mAxesCount; // количество осей авто
    private int iAxle; // индекс оси

    private PartOfCar mCar; // текущая часть машины
    private EditText mWeightEditText; // поле для ввода масс в диалоговом окне

    public CalibrationDialog(String[] weightsTractor, String[] weightsTrailer) {
        mWeightsTractor = weightsTractor;
        mWeightsTrailer = weightsTrailer;
        mAxesTractorCount = weightsTractor.length;
        int mAxesTrailerCount = weightsTrailer.length;
        mAxesCount = mAxesTractorCount + mAxesTrailerCount;

        mCar = mAxesTractorCount > 0 ? PartOfCar.TRACTOR : PartOfCar.TRAILER;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        TRACTOR_TITLE = getResources().getString(R.string.tractor);
        TRAILER_TITLE = getResources().getString(R.string.trailer);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        iAxle = 1;
        FrameLayout container = createViewWithText(getContext(), ""); // text replace below
        mWeightEditText = container.findViewById(R.id.edit_text_field);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.icon)
                .setTitle("Title") // replace below
                .setView(container)
                .setNeutralButton(R.string.dialog_button_break_w, null)
                .setPositiveButton(R.string.dialog_button_next, null)
                .setNegativeButton(R.string.dialog_button_back, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            AlertDialog alertDialog = (AlertDialog) d;
            changeButtonVisible(alertDialog);
            updateView(alertDialog);
        });
        return dialog;
    }

    private String getWeightTrailer(int i) {
        return mWeightsTrailer[i - 1];
    }

    private String getWeightTractor(int i) {
        return mWeightsTractor[i - 1];
    }

    private void saveWeight() {
        if (mCar.equals(PartOfCar.TRACTOR)) {
            mWeightsTractor[iAxle - 1] = mWeightEditText.getText().toString();
        } else {
            mWeightsTrailer[iAxle - mAxesTractorCount - 1] = mWeightEditText.getText().toString();
        }
    }

    private String createTitleForDialogTractor(int i) {
        return TRACTOR_TITLE.concat(": ").concat(String.valueOf(i));
    }

    private String createTitleForDialogTrailer(int i) {
        return TRAILER_TITLE.concat(": ").concat(String.valueOf(i));
    }

    private void changeButtonVisible(AlertDialog dialog) {
        if (iAxle == mAxesCount) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.dialog_button_save);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.dialog_button_next);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
        }

        if (iAxle > 1) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
        } else {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> onDialogPositiveClick(CalibrationDialog.this));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener(v -> onDialogNegativeClick(CalibrationDialog.this));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener(v -> onDialogNeutralButton(CalibrationDialog.this));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        saveWeight();
        iAxle++;

        if (iAxle > mAxesCount) {
            dialog.dismiss();
            getListener().onDialogPositiveClick(dialog);
        } else {
            updateCarType();
            AlertDialog alertDialog = (AlertDialog) dialog.getDialog();
            changeButtonVisible(alertDialog);
            updateView(alertDialog);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        saveWeight();
        iAxle--;
        updateCarType();
        AlertDialog alertDialog = (AlertDialog) dialog.getDialog();
        changeButtonVisible(alertDialog);
        updateView(alertDialog);
    }

    @Override
    public void onDialogNeutralButton(DialogFragment dialog) {
        saveWeight();
        dialog.dismiss();
        getListener().onDialogNegativeClick(dialog);
    }

    private void updateView(AlertDialog dialog) {
        if (mCar.equals(PartOfCar.TRACTOR)) {
            int index = iAxle;
            dialog.setTitle(createTitleForDialogTractor(index));
            mWeightEditText.setText(getWeightTractor(index));
        } else {
            int index = iAxle - mAxesTractorCount;
            dialog.setTitle(createTitleForDialogTrailer(index));
            mWeightEditText.setText(getWeightTrailer(index));
        }
        mWeightEditText.selectAll();
    }

    private void updateCarType() {
        boolean tractor = iAxle <= mAxesTractorCount;
        mCar = tractor ? PartOfCar.TRACTOR : PartOfCar.TRAILER;
    }

    public String[] getTractorWeights() {
        return mWeightsTractor;
    }

    public String[] getTrailerWeights() {
        return mWeightsTrailer;
    }
}
