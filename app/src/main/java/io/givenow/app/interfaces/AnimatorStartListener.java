package io.givenow.app.interfaces;

import android.animation.Animator;

/**
 * Created by aphex on 11/10/15.
 */
//Don't use - causes strange thread.cc crashes. See notes in CustomAnimations.java
@Deprecated
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