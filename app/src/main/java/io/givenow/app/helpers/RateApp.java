package io.givenow.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by aphex on 12/6/15.
 */
public class RateApp {

    public static void rateNow(Context context) {
        String appPackage = context.getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
