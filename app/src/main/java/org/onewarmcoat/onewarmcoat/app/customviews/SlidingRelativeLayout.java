package org.onewarmcoat.onewarmcoat.app.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * Created by paveld on 4/13/14.
 * https://github.com/paveldudka/TranslateFragment.git
 */
public class SlidingRelativeLayout extends RelativeLayout {

    private float yFraction = 0;
    private ViewTreeObserver.OnPreDrawListener preDrawListener = null;

    public SlidingRelativeLayout(Context context) {
        super(context);
    }

    public SlidingRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public float getYFraction() {
        return this.yFraction;
    }

    public void setYFraction(float fraction) {

        this.yFraction = fraction;

        if (getHeight() == 0) {
            if (preDrawListener == null) {
                preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                        setYFraction(yFraction);
                        return true;
                    }
                };
                getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
            return;
        }

        float translationY = getHeight() * fraction;
        setTranslationY(translationY);
    }
}