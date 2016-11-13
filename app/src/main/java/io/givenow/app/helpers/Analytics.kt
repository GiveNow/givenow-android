package io.givenow.app.helpers

import android.util.Log
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.givenow.app.models.ParseUserHelper
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by aphex on 12/28/15.

 */
object Analytics {
    fun sendHit(tracker: Tracker, category: String, action: String, label: String) {
        tracker.send(HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build())
    }

    fun mixpanelTrackIsRegisteredUser(mixpanel: MixpanelAPI, eventName: String) {
        try {
            val props = JSONObject()
            props.put("IsRegistered", ParseUserHelper.isRegistered)
            mixpanel.track(eventName, props)
        } catch (e: JSONException) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e)
        }

    }
}
