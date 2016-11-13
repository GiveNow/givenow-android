package io.givenow.app.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import io.givenow.app.R

class AdaptableGradientRectView : View {
    private lateinit var gradient: RadialGradient

    @ColorInt var gradientColorFrom: Int
        get
        set(@ColorInt color) {
            field = color
            invalidate()
        }

    @ColorInt var gradientColorTo: Int
        get
        set(@ColorInt color) {
            field = color
            invalidate()
        }

    init {
        gradientColorFrom = resources.getColor(android.R.color.white)
        gradientColorTo = resources.getColor(R.color.colorPrimaryLight)
    }

    constructor(context: Context) : super(context, null) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AdaptableGradientRectView, 0, 0)
        try {
            gradientColorFrom = ta.getColor(R.styleable.AdaptableGradientRectView_gradientColorFrom, gradientColorFrom)
            gradientColorTo = ta.getColor(R.styleable.AdaptableGradientRectView_gradientColorTo, gradientColorTo)
        } finally {
            ta.recycle()
        }
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val maxSize = Math.max(height, width)

        gradient = RadialGradient(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                (maxSize * 60 / 100).toFloat(),
                intArrayOf(gradientColorFrom and 0xfffffff, gradientColorTo), //alpha, red, green, blue)},
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP
        )

        paint.isDither = true
        paint.shader = gradient

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
