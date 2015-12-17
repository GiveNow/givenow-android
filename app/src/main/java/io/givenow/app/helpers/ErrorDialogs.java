package io.givenow.app.helpers;

import android.content.Context;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import io.givenow.app.R;

/**
 * Created by aphex on 12/3/15.
 */
public class ErrorDialogs {
    public static void connectionFailure(Context context, Throwable error) {
        Log.d("ErrorDialogs", "Showing connectionFailure dialog for throwable: " + error.getMessage());
        error.printStackTrace();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error_connection_failure_title)
                    .setMessage(R.string.error_connection_failure_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show();
        } else {
            Log.e("ErrorDialogs", "Not on main looper! Could not show connectionFailure dialog for throwable: " + error.getMessage());
        }
    }
}
