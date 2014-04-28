package org.onewarmcoat.onewarmcoat.app.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.view.View;

public class AdaptableGradientRectView extends View {
    public AdaptableGradientRectView(Context context) {
        super(context);
    }

    public AdaptableGradientRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdaptableGradientRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int maxSize = Math.max(getHeight(), getWidth());
        RadialGradient gradient = new RadialGradient(
                getWidth() / 2,
                getHeight() / 2,
                maxSize * 60 / 100,
                new int[]{Color.argb(0x00, 0xFF, 0xFF, 0xFF),
                        Color.argb(0xFF, 0xDD, 0xE8, 0xED)},
                new float[]{0, 1},
                android.graphics.Shader.TileMode.CLAMP
        );

        paint.setDither(true);
        paint.setShader(gradient);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
