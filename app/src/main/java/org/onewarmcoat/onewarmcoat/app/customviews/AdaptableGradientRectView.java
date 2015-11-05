package org.onewarmcoat.onewarmcoat.app.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.onewarmcoat.onewarmcoat.app.R;

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
        ColorDrawable cd = (ColorDrawable) ContextCompat.getDrawable(getContext(), R.color.colorPrimaryLight);
        int color = cd.getColor();
        int alpha = cd.getAlpha();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        RadialGradient gradient = new RadialGradient(
                getWidth() / 2,
                getHeight() / 2,
                maxSize * 80 / 100,
                new int[]{Color.argb(0x00, 0xFF, 0xFF, 0xFF),
                        Color.argb(alpha, red, green, blue)},
                new float[]{0, 1},
                android.graphics.Shader.TileMode.CLAMP
        );

        paint.setDither(true);
        paint.setShader(gradient);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
