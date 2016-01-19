package io.givenow.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
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
        JSONObject pushDataObject = getPushData(intent);
        if (pushDataObject == null || (!pushDataObject.has("data"))) {
            return null;
        }
        JSONObject pushData = pushDataObject.optJSONObject("data");

        String defaultTitle = pushData.optString("title", context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString());
        String defaultAlert = pushData.optString("alert", "Notification received.");

        String title;
        String alert;
        switch (pushData.optString("type")) {
            case "claimPickupRequest":
                // build title
                title = Option.fromNull(pushData.optJSONObject("title")).bind(titleObject ->
                        getLocalizedStringFromObject(context, titleObject))
                        .orSome(defaultTitle);
                // build alert
                alert = Option.fromNull(pushData.optJSONObject("alert")).bind(alertObject ->
                        getLocalizedStringFromObject(context,
                                alertObject,
                                Collections.singletonList(context.getString(
                                        R.string.push_notif_volunteer_default_name))))
                        .orSome(defaultAlert);
                break;
            case "confirmVolunteer":
                // build title
                title = Option.fromNull(pushData.optJSONObject("title")).bind(titleObject ->
                        getLocalizedStringFromObject(context, titleObject))
                        .orSome(defaultTitle);
                // build alert
                alert = Option.fromNull(pushData.optJSONObject("alert")).bind(alertObject ->
                        getLocalizedStringFromObject(context,
                                alertObject))
                        .orSome(defaultAlert);
                break;
            case "pickupDonation":
                // build title
                title = Option.fromNull(pushData.optJSONObject("title")).bind(titleObject ->
                        getLocalizedStringFromObject(context, titleObject))
                        .orSome(defaultTitle);
                // build alert
                alert = Option.fromNull(pushData.optJSONObject("alert")).bind(alertObject ->
                        getLocalizedStringFromObject(context,
                                alertObject,
                                Collections.singletonList(context.getString(
                                        R.string.push_notif_volunteer_default_name))))
                        .orSome(defaultAlert);
                break;
            default:
                title = defaultTitle;
                alert = defaultAlert;
        }

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

    @NonNull
    private Option<String> getLocalizedStringFromObject(Context context, JSONObject jsonObject) {
        return getLocalizedStringFromObject(context, jsonObject, Collections.emptyList());
    }

    @NonNull
    private Option<String> getLocalizedStringFromObject(Context context, JSONObject jsonObject, List<String> defaultArgs) {
        try {
            String locKey = jsonObject.getString("loc-key");
            List<String> locArgs = JsonHelper.toList(jsonObject.getJSONArray("loc-args"));
            if (locArgs.isEmpty()) {
                locArgs.addAll(defaultArgs);
            }
            return Option.some(ResourceHelper.getLocalizedString(context,
                    locKey,
                    locArgs.toArray()));
        } catch (JSONException e) {
            e.printStackTrace();
            return Option.none();
        }
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);
    }
}