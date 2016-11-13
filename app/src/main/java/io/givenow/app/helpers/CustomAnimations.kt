package io.givenow.app.helpers

import android.animation.*
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AnimationUtils
import io.givenow.app.R

//import io.givenow.app.interfaces.AnimatorEndListener;
//import io.givenow.app.interfaces.AnimatorStartListener;

object CustomAnimations {

    fun buttonFlashCTA(obj: Any): ObjectAnimator {
        val anim = ObjectAnimator.ofObject(obj, "backgroundColor", ArgbEvaluator(),
                /*LightBlue*/0xFF246d9e.toInt(), /*Blue*/0xff114880.toInt())
        //                R.color.buttonFlashCTA, R.color.accent);
        anim.duration = 500
        anim.repeatCount = ValueAnimator.INFINITE
        anim.repeatMode = ValueAnimator.REVERSE
        return anim
    }

    fun highlightIncompleteInput(obj: Any): ObjectAnimator {
        val anim = ObjectAnimator.ofObject(obj, "backgroundColor", ArgbEvaluator(),
                /*LightBlue*/0xFFDDE8ED.toInt(), /*Blue*/0xffffffff.toInt())
        anim.duration = 150
        anim.repeatCount = 2
        anim.repeatMode = ValueAnimator.REVERSE
        return anim
    }

    fun animateHeight(v: View, from: Int, to: Int): ValueAnimator {
        val anim = ValueAnimator.ofInt(from, to)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                v.visibility = View.VISIBLE
            }
        })
        anim.addUpdateListener { valueAnimator ->
            val layoutParams = v.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            v.layoutParams = layoutParams
        }
        anim.duration = v.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        return anim
    }

    fun animateWidth(v: View, from: Int, to: Int): ValueAnimator {
        val anim = ValueAnimator.ofInt(from, to)
        /* Once again, a very strange error related to AnimatorStartListener:
        anim.addListener((AnimatorStartListener) animation -> v.setVisibility(View.VISIBLE));
        12-03 01:25:31.049 13819-13819/io.givenow.app A/art: art/runtime/thread.cc:1329] Throwing new exception 'length=4849; index=11412' with unexpected pending exception: java.lang.ArrayIndexOutOfBoundsException: length=4849; index=11412
        12-03 01:25:31.050 13819-13819/io.givenow.app A/art: art/runtime/thread.cc:1329]   at io.givenow.app.interfaces.AnimatorStartListener io.givenow.app.helpers.CustomAnimations$$Lambda$3.lambdaFactory$(android.view.View) ((null):-1)
        12-03 01:25:31.050 13819-13819/io.givenow.app A/art: art/runtime/thread.cc:1329]   at android.animation.ValueAnimator io.givenow.app.helpers.CustomAnimations.animateWidth(android.view.View, int, int) (CustomAnimations.java:53)
         */
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                v.visibility = View.VISIBLE
            }
        })
        anim.addUpdateListener { valueAnimator ->
            val layoutParams = v.layoutParams
            layoutParams.width = valueAnimator.animatedValue as Int
            v.layoutParams = layoutParams
        }
        anim.duration = v.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        return anim
    }

    fun growWidthAndShake(v: View, from: Int, to: Int): Animator {
        val shake = AnimationUtils.loadAnimation(v.context, R.anim.shake)
        val anim = animateWidth(v, from, to)
        anim.duration = v.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                v.startAnimation(shake)
            }
        })
        return anim
    }

    fun circularReveal(v: View): Animator {
        // previously invisible view.
        // get the center for the clipping circle
        val cx = v.width / 2
        val cy = v.height / 2

        // get the final radius for the clipping circle
        val finalRadius = Math.max(v.width, v.height)

        val anim: Animator
        if (Build.VERSION.SDK_INT >= 21) { //ViewAnimationUtils is only API >= 21
            // create the animator for this view (the start radius is zero)
            anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0f, finalRadius.toFloat())
        } else { //Or fall back to a simple fade in
            anim = AnimatorInflater.loadAnimator(v.context, R.animator.fade_in)
            anim.duration = v.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            anim.setTarget(v)
        }

        // make the view visible and start the animation
        /* the following line is a culprit in the following exception:
        anim.addListener((AnimatorStartListener) animation -> v.setVisibility(View.VISIBLE));
        11-26 01:37:19.330 7938-7938/io.givenow.app A/art: art/runtime/thread.cc:1329] Throwing new exception 'length=3413; index=4199' with unexpected pending exception: java.lang.ArrayIndexOutOfBoundsException: length=3413; index=4199
        11-26 01:37:19.331 7938-7938/io.givenow.app A/art: art/runtime/thread.cc:1329]   at io.givenow.app.interfaces.AnimatorStartListener io.givenow.app.helpers.CustomAnimations$$Lambda$5.lambdaFactory$(android.view.View) ((null):-1)
        11-26 01:37:19.331 7938-7938/io.givenow.app A/art: art/runtime/thread.cc:1329]   at android.animation.Animator io.givenow.app.helpers.CustomAnimations.circularReveal(android.view.View) (CustomAnimations.java:77)

        ...so let's use an AnimatorListenerAdapter instead!
        */
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                v.visibility = View.VISIBLE
            }
        })
        return anim
    }

    fun circularHide(v: View): Animator {
        // previously visible view

        // get the center for the clipping circle
        val cx = v.width / 2
        val cy = v.height / 2

        // get the initial radius for the clipping circle
        val initialRadius = v.width

        // create the animation (the final radius is zero)
        val anim: Animator
        if (Build.VERSION.SDK_INT >= 21) { //ViewAnimationUtils is only API >= 21
            anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, initialRadius.toFloat(), 0f)
        } else { //Or fall back to a simple fade out
            anim = AnimatorInflater.loadAnimator(v.context, R.animator.fade_out)
            anim.duration = v.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            anim.setTarget(v)
        }

        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                v.visibility = View.INVISIBLE
            }
        })
        // start the animation
        return anim
    }
}
