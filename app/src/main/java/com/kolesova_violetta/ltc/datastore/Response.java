package com.kolesova_violetta.ltc.datastore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Response<S, T extends Throwable> {
    boolean success = false;

    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Response<?,?> cllbck2 = (Response<?,?>) obj;

        return success == cllbck2.success;
    }

    @NonNull
    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                '}';
    }
}

