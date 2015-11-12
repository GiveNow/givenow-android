package io.givenow.app.interfaces;

import android.animation.Animator;

/**
 * Created by aphex on 11/10/15.
 */
public interface AnimatorStartListener extends Animator.AnimatorListener {
    @Override
    void onAnimationStart(Animator animation);

    @Override
    default void onAnimationEnd(Animator animation) {
    }

    @Override
    default void onAnimationCancel(Animator animation) {

    }

    @Override
    default void onAnimationRepeat(Animator animation) {

    }
}