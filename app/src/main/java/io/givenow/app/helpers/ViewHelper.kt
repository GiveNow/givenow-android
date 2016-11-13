package io.givenow.app.helpers

import android.view.View

/**
 * Created by aphex on 3/20/16.
 *
 */
object ViewHelper {
    fun safeVisible(view: View?) {
        view?.visibility = View.VISIBLE
    }

    fun safeInvisible(view: View?) {
        view?.visibility = View.INVISIBLE
    }

    fun safeGone(view: View?) {
        view?.visibility = View.GONE
    }
}

