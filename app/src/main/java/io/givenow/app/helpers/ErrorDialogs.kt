package io.givenow.app.helpers

import android.content.Context
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.util.Log
import io.givenow.app.R

/**
 * Created by aphex on 12/3/15.

 */
object ErrorDialogs {
    fun connectionFailure(context: Context, error: Throwable) {
        Log.d("ErrorDialogs", "Showing connectionFailure dialog for throwable: " + error.message)
        error.printStackTrace()
        error.cause?.let { cause ->
            Log.d("ErrorDialogs", "Error caused by: ")
            cause.printStackTrace()
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.error_connection_failure_title)
                    .setMessage(R.string.error_connection_failure_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show()
        } else {
            Log.e("ErrorDialogs", "Not on main looper! Could not show connectionFailure dialog for throwable: " + error.message)
        }
    }
}
