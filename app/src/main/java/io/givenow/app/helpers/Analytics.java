package io.givenow.app.helpers;

import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import io.givenow.app.models.ParseUserHelper;

/**
 * Created by aphex on 12/28/15.
 */
public class Analytics {
    public static void sendHit(Tracker tracker, String category, String action, String label) {
        tracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

    public static void mixpanelTrackIsRegisteredUser(MixpanelAPI mixpanel, String eventName) {
        try {
            JSONObject props = new JSONObject();
            props.put("IsRegistered", ParseUserHelper.isRegistered());
            mixpanel.track(eventName, props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }

    }
}
