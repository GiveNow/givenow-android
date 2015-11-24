package io.givenow.app.interfaces;

import android.animation.Animator;

/**
 * Created by aphex on 11/10/15.
 */
public abstract class AnimatorEndListener implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(Animator animation) {
        //Log.e("AEL", "STARTED" + animation);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        //Log.e("AEL", "CANCELED" + animation);
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}