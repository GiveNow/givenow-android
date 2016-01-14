package io.givenow.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

import fj.data.Option;
import io.givenow.app.helpers.JsonHelper;
import io.givenow.app.helpers.ResourceHelper;

/**
 * Created by aphex on 1/13/16.
 */
public class GiveNowPushReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = getClass().getSimpleName();

    @Override
    public void onPushReceive(Context context, Intent intent) {

        String pushDataStr = intent.getStringExtra(KEY_PUSH_DATA);
        if (pushDataStr == null) {
            Log.e(TAG, "Can not get push data from intent.");
            return;
        }
        Log.v(TAG, "Received push data: " + pushDataStr);

        JSONObject pushData = null;
        try {
            pushData = new JSONObject(pushDataStr);
        } catch (JSONException e) {
            Log.e(TAG, "Unexpected JSONException when receiving push data: ", e);
        }


//        Option.fromNull(pushData).foreachDoEffect(push -> {
//            Bundle extras = intent.getExtras();
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.putExtras(extras);
//            // If the push data includes an action string, that broadcast intent is fired.
//            Option.fromNull(push.optString("action", null)).foreachDoEffect(
//                    broadcastIntent::setAction);
//            broadcastIntent.setPackage(context.getPackageName());
//            context.sendBroadcast(broadcastIntent);
//
//        });
        String action = null;
        if (pushData != null) {
            action = pushData.optString("action", null);
        }
        if (action != null) {
            Bundle extras = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(extras);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }
        Option.fromNull(getNotification(context, intent)).foreachDoEffect(notification ->
                GiveNowNotificationManager.getInstance().showNotification(context, notification));
    }

    private JSONObject getPushData(Intent intent) {
        try {
            return new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
        } catch (JSONException e) {
            Log.e(TAG, "Unexpected JSONException when receiving push data: ", e);
            return null;
        }
    }

    /* Copied from ParsePushBroadcastReceiver, enhanced with alert localization. */
    @Override
    protected Notification getNotification(Context context, Intent intent) {
        JSONObject pushData = getPushData(intent);
        if (pushData == null || (!pushData.has("alert") && !pushData.has("title"))) {
            return null;
        }

        String defaultTitle = pushData.optString("title", context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString());
        String title =
                Option.fromNull(pushData.optJSONObject("title")).option(
                        defaultTitle,
                        titleObject -> {
                            try {
                                return ResourceHelper.getLocalizedString(context,
                                        titleObject.getString("loc-key"),
                                        JsonHelper.toList(titleObject.getJSONArray("loc-args")).toArray());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return defaultTitle;
                            }
                        }
                );

        String defaultAlert = pushData.optString("alert", "Notification received.");
        String alert =
                Option.fromNull(pushData.optJSONObject("alert")).option(
                        defaultAlert,
                        alertObject -> {
                            try {
                                return ResourceHelper.getLocalizedString(context,
                                        alertObject.getString("loc-key"),
                                        JsonHelper.toList(alertObject.getJSONArray("loc-args")).toArray());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return defaultAlert;
                            }
                        }
                );

        String tickerText = String.format(Locale.getDefault(), "%s: %s", title, alert);

        Bundle extras = intent.getExtras();

        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();

        // Security consideration: To protect the app from tampering, we require that intent filters
        // not be exported. To protect the app from information leaks, we restrict the packages which
        // may intercept the push intents.
        String packageName = context.getPackageName();

        Intent contentIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);

        Intent deleteIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The purpose of setDefaults(Notification.DEFAULT_ALL) is to inherit notification properties
        // from system defaults
        NotificationCompat.Builder parseBuilder = new NotificationCompat.Builder(context);
        parseBuilder.setContentTitle(title)
                .setContentText(alert)
                .setTicker(tickerText)
                .setSmallIcon(this.getSmallIconId(context, intent))
                .setLargeIcon(this.getLargeIcon(context, intent))
                .setContentIntent(pContentIntent)
                .setDeleteIntent(pDeleteIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);
        return parseBuilder.build();
    }

//    private String localizeObjectOrDefault(String key, String defaultString) {
//        return defaultString;
//    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);
    }
}