package com.iisu.assettool.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF

/**
 * Image processing utilities for Android.
 * Ports the Python PIL/OpenCV logic to Android Bitmap operations.
 */
class ImageProcessor {

    /**
     * Apply a border/frame to an image.
     * The border image should have a transparent area where the content goes.
     */
    fun applyBorder(image: Bitmap, border: Bitmap): Bitmap {
        // Create output bitmap matching border size
        val output = Bitmap.createBitmap(
            border.width,
            border.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(output)

        // Calculate scaling and positioning for the image
        // Center the image within the border's content area
        val contentRect = findContentArea(border)
        val scaledImage = scaleBitmapToFit(image, contentRect.width(), contentRect.height())

        // Center the scaled image in the content area
        val left = contentRect.left + (contentRect.width() - scaledImage.width) / 2
        val top = contentRect.top + (contentRect.height() - scaledImage.height) / 2

        // Draw the image first
        canvas.drawBitmap(scaledImage, left.toFloat(), top.toFloat(), null)

        // Draw the border on top
        canvas.drawBitmap(border, 0f, 0f, null)

        return output
    }

    /**
     * Find the transparent/content area in a border image.
     * Analyzes the border to find where content should be placed.
     */
    private fun findContentArea(border: Bitmap): Rect {
        var minX = border.width
        var minY = border.height
        var maxX = 0
        var maxY = 0

        // Scan for transparent pixels
        for (x in 0 until border.width) {
            for (y in 0 until border.height) {
                val pixel = border.getPixel(x, y)
                val alpha = (pixel shr 24) and 0xFF

                // If pixel is mostly transparent, it's part of the content area
                if (alpha < 128) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }

        // If no transparent area found, use center 80% of the image
        if (maxX <= minX || maxY <= minY) {
            val margin = (border.width * 0.1).toInt()
            return Rect(
                margin,
                margin,
                border.width - margin,
                border.height - margin
            )
        }

        return Rect(minX, minY, maxX, maxY)
    }

    /**
     * Scale a bitmap to fit within the given dimensions while maintaining aspect ratio.
     */
    private fun scaleBitmapToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val widthRatio = maxWidth.toFloat() / bitmap.width
        val heightRatio = maxHeight.toFloat() / bitmap.height
        val ratio = minOf(widthRatio, heightRatio)

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Create a rounded corner version of the image.
     */
    fun roundCorners(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
        }

        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

        // Draw rounded rect
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        // Set xfermode to draw image only where we drew
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return output
    }

    /**
     * Resize an image to specific dimensions.
     */
    fun resize(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * Create a square version of an image by center cropping.
     */
    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
}
