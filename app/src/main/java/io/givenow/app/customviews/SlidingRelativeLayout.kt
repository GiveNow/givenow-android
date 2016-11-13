package io.givenow.app.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.RelativeLayout

/**
 * Created by paveld on 4/13/14.
 * https://github.com/paveldudka/TranslateFragment.git
 */
class SlidingRelativeLayout : RelativeLayout {

    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    var yFraction = 0f
        get
        set(fraction) {

            field = fraction

            if (height == 0) {
                if (preDrawListener == null) {
                    preDrawListener = ViewTreeObserver.OnPreDrawListener {
                        viewTreeObserver.removeOnPreDrawListener(preDrawListener)
                        yFraction = this.yFraction //apparently this is to trigger the setter again. hacky but i'll take it for now
                        true
                    }

                    viewTreeObserver.addOnPreDrawListener(preDrawListener)
                }
            }

            val translationY = height * fraction
            setTranslationY(translationY)
        }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    }
}