package io.givenow.app.helpers;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by aphex on 12/28/15.
 */
public class Analytics {
    public static void sendHit(Tracker tracker, String category, String action, String label) {
        tracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }
}
