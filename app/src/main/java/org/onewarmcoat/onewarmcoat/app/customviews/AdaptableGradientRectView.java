package org.onewarmcoat.onewarmcoat.app.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.view.View;

import org.onewarmcoat.onewarmcoat.app.R;

public class AdaptableGradientRectView extends View {
    private int gradientColorFrom = getResources().getColor(android.R.color.white);
    private int gradientColorTo = getResources().getColor(R.color.colorPrimaryLight);
    private RadialGradient mGradient;

    public AdaptableGradientRectView(Context context) {
        super(context, null);
    }

    public AdaptableGradientRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AdaptableGradientRectView, 0, 0);
        try {
            gradientColorFrom = ta.getColor(R.styleable.AdaptableGradientRectView_gradientColorFrom, gradientColorFrom);
            gradientColorTo = ta.getColor(R.styleable.AdaptableGradientRectView_gradientColorTo, gradientColorTo);
        } finally {
            ta.recycle();
        }
    }

    public int getGradientColorTo() {
        return gradientColorTo;
    }

    public void setGradientColorTo(int gradientColorTo) {
        this.gradientColorTo = gradientColorTo;
        invalidate();
    }

    public int getGradientColorFrom() {
        return gradientColorFrom;
    }

    public void setGradientColorFrom(int gradientColorFrom) {
        this.gradientColorFrom = gradientColorFrom;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int maxSize = Math.max(getHeight(), getWidth());

        mGradient = new RadialGradient(
                getWidth() / 2,
                getHeight() / 2,
                maxSize * 60 / 100,
                new int[]{gradientColorFrom & 0xfffffff,
                        gradientColorTo},//alpha, red, green, blue)},
                new float[]{0, 1},
                android.graphics.Shader.TileMode.CLAMP
        );

        paint.setDither(true);
        paint.setShader(mGradient);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
