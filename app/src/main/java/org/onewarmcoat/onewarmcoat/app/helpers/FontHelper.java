package org.onewarmcoat.onewarmcoat.app.helpers;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

public class FontHelper {
    private static Hashtable fontCache = new Hashtable();

    public static Typeface getTypeface(Context context, String font) {
        Typeface typeface = (Typeface) fontCache.get(font);
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), font);
            fontCache.put(font, typeface);
        }
        return typeface;
    }
}
// use this like this:
//         mFont = FontHelper.getTypeface(context.getApplicationContext(), "fonts/Roboto-Light.ttf");
