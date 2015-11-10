package io.givenow.app.helpers;

import android.util.Log;

/**
 * Helps debug the lamentable Google Play Services 6.5.87 maps crash by
 * providing visibility into how many map fragments are currently running.
 */
public class MapFragmentCounter {
    private static int count = 0;
    private static MapFragmentCounter ourInstance = new MapFragmentCounter();

    private MapFragmentCounter() {
    }

    public static MapFragmentCounter getInstance() {
        return ourInstance;
    }

    public static void inc() {
        count += 1;
        Log.e(getInstance().getClass().getSimpleName(), String.valueOf(count));
        if (count > 1) {
            Log.e(getInstance().getClass().getSimpleName(), "WARNING: MORE THAN 1 MAPFRAGMENT ACTIVE: " + String.valueOf(count));
//            throw new RuntimeException(String.valueOf(count));
        }
    }

    public static void dec() {
        count -= 1;
        Log.e(getInstance().getClass().getSimpleName(), String.valueOf(count));
    }

    public static int getCount() {
        return count;
    }
}
