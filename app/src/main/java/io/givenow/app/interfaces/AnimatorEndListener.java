package io.givenow.app.interfaces;

import android.animation.Animator;
import android.util.Log;

/**
 * Created by aphex on 11/10/15.
 */
public interface AnimatorEndListener extends Animator.AnimatorListener {
    @Override
    default void onAnimationStart(Animator animation) {
        Log.e("AEL", "STARTED" + animation);
    }

    @Override
    void onAnimationEnd(Animator animation);

    @Override
    default void onAnimationCancel(Animator animation) {
        Log.e("AEL", "CANCELED" + animation);
    }

    @Override
    default void onAnimationRepeat(Animator animation) {

    }
}