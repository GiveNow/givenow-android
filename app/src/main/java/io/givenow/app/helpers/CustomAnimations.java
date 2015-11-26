package io.givenow.app.helpers;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import io.givenow.app.R;
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

        Animator anim;
        if (android.os.Build.VERSION.SDK_INT >= 21) { //ViewAnimationUtils is only API >= 21
            // create the animator for this view (the start radius is zero)
            anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
        } else { //Or fall back to a simple fade in
            anim = AnimatorInflater.loadAnimator(v.getContext(), R.animator.fade_in);
        }

        // make the view visible and start the animation
//        anim.addListener((AnimatorStartListener) animation -> v.setVisibility(View.VISIBLE));
        //the above line is a culprit in the following exception:
//        11-26 01:37:19.330 7938-7938/io.givenow.app A/art: art/runtime/thread.cc:1329] Throwing new exception 'length=3413; index=4199' with unexpected pending exception: java.lang.ArrayIndexOutOfBoundsException: length=3413; index=4199
//        11-26 01:37:19.331 7938-7938/io.givenow.app A/art: art/runtime/thread.cc:1329]   at io.givenow.app.interfaces.AnimatorStartListener io.givenow.app.helpers.CustomAnimations$$Lambda$5.lambdaFactory$(android.view.View) ((null):-1)
//        11-26 01:37:19.331 7938-7938/io.givenow.app A/art: art/runtime/thread.cc:1329]   at android.animation.Animator io.givenow.app.helpers.CustomAnimations.circularReveal(android.view.View) (CustomAnimations.java:77)
        //so let's use an AnimatorListenerAdapter instead!
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                v.setVisibility(View.VISIBLE);
            }
        });
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
        Animator anim;
        if (android.os.Build.VERSION.SDK_INT >= 21) { //ViewAnimationUtils is only API >= 21
            anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, initialRadius, 0);
        } else { //Or fall back to a simple fade out
            anim = AnimatorInflater.loadAnimator(v.getContext(), R.animator.fade_out);
        }

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
