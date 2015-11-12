package io.givenow.app.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import io.givenow.app.R;

/**
 * Created by aphex on 11/12/15.
 */
public class AttributeGetter {

    public static int getDimensionAttr(Context context, int id) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(id, typedValue, true);
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.actionBarSize});
        int attr = typedArray.getDimensionPixelSize(0, -1);
        typedArray.recycle();
        return attr;
    }
}
