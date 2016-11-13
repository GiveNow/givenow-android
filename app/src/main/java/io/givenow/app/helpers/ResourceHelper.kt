package io.givenow.app.helpers

import android.content.Context
import android.util.TypedValue
import io.givenow.app.R

/**
 * Created by aphex on 11/12/15
 */
object ResourceHelper {

    fun getDimensionAttr(context: Context, id: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(id, typedValue, true)
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.actionBarSize))
        val attr = typedArray.getDimensionPixelSize(0, -1)
        typedArray.recycle()
        return attr
    }
}
