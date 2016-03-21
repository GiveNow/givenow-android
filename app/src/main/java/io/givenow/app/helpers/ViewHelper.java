package io.givenow.app.helpers;

import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by aphex on 3/20/16.
 */
public class ViewHelper {
    public static void safeVisible(@Nullable View view) {
        safeViewVisibility(view, View.VISIBLE);
    }

    public static void safeInvisible(@Nullable View view) {
        safeViewVisibility(view, View.INVISIBLE);
    }

    public static void safeGone(@Nullable View view) {
        safeViewVisibility(view, View.GONE);
    }

    private static void safeViewVisibility(@Nullable View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }
}

