package io.givenow.app.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import io.givenow.app.R;

/**
 * Created by aphex on 11/12/15
 */
public class ResourceHelper {

    public static int getDimensionAttr(Context context, int id) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(id, typedValue, true);
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.actionBarSize});
        int attr = typedArray.getDimensionPixelSize(0, -1);
        typedArray.recycle();
        return attr;
    }

    @NonNull
    public static String getLocalizedString(Context context, String id, Object... args) {
        int stringRes = context.getResources().getIdentifier(id, "string", context.getPackageName());
        return context.getString(stringRes, args);
    }
}
