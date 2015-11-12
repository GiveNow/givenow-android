package io.givenow.app.helpers;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
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
}
