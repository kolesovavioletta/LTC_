package com.kolesova_violetta.ltc.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.kolesova_violetta.ltc.R;

import java.util.ArrayList;
import java.util.List;

public class UiHelper {
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static ProgressDialog createWaitProgressDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setTitle(R.string.wait_please);
        pd.setCancelable(false);
        return pd;
    }

    public static String[] selectNotGrantedPermissions(String[] neededPermissions, Context context) {
        List<String> requestPermission = new ArrayList<>();
        for(String perm : neededPermissions) {
            if (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_DENIED) {
                requestPermission.add(perm);
            }
        }
        return requestPermission.toArray(new String[0]);
    }
}
