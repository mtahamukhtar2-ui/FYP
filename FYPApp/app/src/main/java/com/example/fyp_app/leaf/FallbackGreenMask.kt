package com.example.fyp_app.leaf

import android.graphics.Bitmap
import android.graphics.Color

object FallbackGreenMask {
    /**
     * A robust color-based fallback for leaf isolation when ML Kit is unavailable.
     * Detects healthy green, yellowing, and brown diseased areas.
     */
    fun isolateLeaf(input: Bitmap): Bitmap {
        val width = input.width
        val height = input.height
        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)

        val hsv = FloatArray(3)
        for (i in pixels.indices) {
            val color = pixels[i]
            Color.colorToHSV(color, hsv)
            val hue = hsv[0]
            val sat = hsv[1]
            val value = hsv[2]

            // Healthy Green
            val isGreen = hue in 65f..170f && sat > 0.15f && value > 0.15f
            // Yellowing / Spiny Whitefly
            val isYellow = hue in 40f..65f && sat > 0.2f && value > 0.3f
            // Brown / Die Back / Necrosis
            val isBrown = hue in 10f..40f && sat > 0.15f && value > 0.15f
            // Powdery Mildew (White/Grey patches)
            val isWhitePatch = sat < 0.2f && value > 0.5f && (Color.green(color) > Color.blue(color))

            if (!(isGreen || isYellow || isBrown || isWhitePatch)) {
                pixels[i] = Color.WHITE
            }
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }
}
