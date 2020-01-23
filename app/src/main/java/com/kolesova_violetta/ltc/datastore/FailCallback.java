package com.kolesova_violetta.ltc.datastore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kolesova_violetta.ltc.BuildConfig;

public class FailCallback<S, F extends Throwable> extends Response<S, F> {
    private F error;

    public FailCallback() {
        success = false;
    }

    public FailCallback(F error) {
        this();
        setError(error);
    }

    public F getError() {
        return error;
    }

    public void setError(F error) {
        if(BuildConfig.DEBUG) {
            if(error == null) {
                Log.e("!@#$", "FailCallback:setError:26: Ошибка не задана");
            } else {
                error.printStackTrace();
            }
        }
        this.error = error;
    }

    @NonNull
    @Override
    public String toString() {
        if(error != null) {
            return error.toString();
        } else {
            return "empty error";
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        FailCallback<?,?> cllbck2 = (FailCallback<?,?>) obj;

        if(error == null || null == cllbck2.error) {
            return error == cllbck2.error;
        }

        return error.getClass().equals(cllbck2.error.getClass()) /*&& error.equals(cllbck2.error)*/;
    }
}
