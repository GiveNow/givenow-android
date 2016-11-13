package io.givenow.app.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Created by aphex on 12/6/15.
 *
 */
object RateApp {

    fun rateNow(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
