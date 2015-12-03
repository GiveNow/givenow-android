package io.givenow.app.interfaces;

import android.animation.Animator;

/**
 * Created by aphex on 11/10/15.
 */
// Don't use - causes strange lambda behavior, and onAnimationEnd is called twice. See earlier commit history of this file.
@Deprecated
public interface AnimatorEndListener extends Animator.AnimatorListener {
    @Override
    default void onAnimationStart(Animator animation) {

    }

    @Override
    void onAnimationEnd(Animator animation);

    @Override
    default void onAnimationCancel(Animator animation) {

    }

    @Override
    default void onAnimationRepeat(Animator animation) {

    }
}