package com.iisu.assettool.ui

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * Custom drawable that renders a dot grid pattern.
 * Matches the iiSU Network desktop aesthetic.
 */
class DotGridDrawable(
    private val dotColor: Int = 0x1500D4FF.toInt(),
    private val dotRadius: Float = 1.5f,
    private val spacing: Float = 24f
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dotColor
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.isEmpty) return

        var y = spacing / 2
        while (y < bounds.height()) {
            var x = spacing / 2
            while (x < bounds.width()) {
                canvas.drawCircle(x, y, dotRadius, paint)
                x += spacing
            }
            y += spacing
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    fun setDotColor(color: Int) {
        paint.color = color
        invalidateSelf()
    }
}
