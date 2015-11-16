package io.givenow.app.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import io.givenow.app.interfaces.AnimatorStartListener;

public class CustomAnimations {

    public static ObjectAnimator buttonFlashCTA(Object obj) {
        ObjectAnimator anim = ObjectAnimator.ofObject(obj, "backgroundColor", new ArgbEvaluator(),
                          /*LightBlue*/0xFF246d9e, /*Blue*/0xff114880);
//                R.color.buttonFlashCTA, R.color.accent);
        anim.setDuration(500).setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        return anim;
    }

    public static ObjectAnimator highlightIncompleteInput(Object obj) {
        ObjectAnimator anim = ObjectAnimator.ofObject(obj, "backgroundColor", new ArgbEvaluator(),
          /*LightBlue*/0xFFDDE8ED, /*Blue*/0xffffffff);
        anim.setDuration(150).setRepeatCount(2);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        return anim;
    }

    public static ValueAnimator animateHeight(View v, int from, int to) {
        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.addListener((AnimatorStartListener) animation -> v.setVisibility(View.VISIBLE));
        anim.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
            v.setLayoutParams(layoutParams);
        });
        anim.setDuration(v.getResources().getInteger(android.R.integer.config_longAnimTime));
        return anim;
    }

    public static ValueAnimator animateWidth(View v, int from, int to) {
        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.addListener((AnimatorStartListener) animation -> v.setVisibility(View.VISIBLE));
        anim.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            layoutParams.width = (Integer) valueAnimator.getAnimatedValue();
            v.setLayoutParams(layoutParams);
        });
        anim.setDuration(v.getResources().getInteger(android.R.integer.config_longAnimTime));
        return anim;
    }

    public static Animator circularReveal(View v) {
        // previously invisible view.
        // get the center for the clipping circle
        int cx = v.getWidth() / 2;
        int cy = v.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(v.getWidth(), v.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        anim.addListener((AnimatorStartListener) animation -> v.setVisibility(View.VISIBLE));
        return anim;
    }

    public static Animator circularHide(View v) {
        // previously visible view

        // get the center for the clipping circle
        int cx = v.getWidth() / 2;
        int cy = v.getHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = v.getWidth();

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(v, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setVisibility(View.INVISIBLE);
            }
        });
        // start the animation
        return anim;
    }
}
