package io.givenow.app.helpers;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import io.givenow.app.R;

/**
 * Created by aphex on 12/3/15.
 */
public class ErrorDialogs {
    private static Context context = null;

    public static void registerContext(Context ctx) {
        context = ctx;
    }

    public static void connectionFailure(Throwable error) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.error_connection_failure)
                .setMessage(error.getMessage())
                .setIcon(android.R.attr.alertDialogIcon)
                .show();
    }
}
