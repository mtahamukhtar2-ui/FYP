package com.example.fyp_app.leaf

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import java.util.ArrayDeque
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LeafBackgroundRemover {

    /**
     * Removes the background from a leaf image using ML Kit Subject Segmentation
     * combined with hybrid color masking to preserve diseased areas.
     */
    suspend fun removeBackground(input: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        // 1. Downscale for processing to save memory and improve speed
        val maxDim = 1024
        val scale = if (input.width > maxDim || input.height > maxDim) {
            maxDim.toFloat() / maxOf(input.width, input.height)
        } else 1f

        val scaledInput = if (scale < 1f) {
            Bitmap.createScaledBitmap(input, (input.width * scale).toInt(), (input.height * scale).toInt(), true)
        } else input

        val result = try {
            runMlKit(scaledInput)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val mask = result?.foregroundConfidenceMask

        val processed = if (mask != null) {
            applyHybridMask(scaledInput, mask)
        } else {
            FallbackGreenMask.isolateLeaf(scaledInput)
        }

        val cleaned = cleanIsolatedBlobs(processed)
        val finalResult = cropToLeaf(cleaned)

        // Cleanup intermediate bitmaps if they were created
        if (scaledInput !== input) scaledInput.recycle()
        if (processed !== scaledInput && processed !== cleaned) processed.recycle()
        if (cleaned !== processed && cleaned !== finalResult) cleaned.recycle()

        finalResult
    }

    private suspend fun runMlKit(
        input: Bitmap
    ): SubjectSegmentationResult? =
        suspendCancellableCoroutine<SubjectSegmentationResult?> { cont ->
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundConfidenceMask()
                .build()
            val segmenter = SubjectSegmentation.getClient(options)
            val image = InputImage.fromBitmap(input, 0)

            segmenter.process(image)
                .addOnSuccessListener { result ->
                    cont.resume(result)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
                .addOnCompleteListener {
                    segmenter.close()
                }

            cont.invokeOnCancellation { segmenter.close() }
        }

    private fun applyHybridMask(original: Bitmap, maskBuffer: FloatBuffer): Bitmap {
        val width = original.width
        val height = original.height
        val pixels = IntArray(width * height)
        original.getPixels(pixels, 0, width, 0, 0, width, height)

        maskBuffer.rewind()
        val hsv = FloatArray(3)

        for (i in pixels.indices) {
            // Check if maskBuffer has remaining before getting to avoid BufferUnderflowException
            val confidence = if (maskBuffer.hasRemaining()) maskBuffer.get() else 0f
            val color = pixels[i]

            // Fast exit for already white pixels (if any)
            if (color == Color.WHITE) continue

            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            Color.colorToHSV(color, hsv)
            val hue = hsv[0]
            val sat = hsv[1]
            val value = hsv[2]

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // LEAF COLOR DETECTION (Widened Ranges)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            val isHealthyGreen = hue in 60f..175f && sat > 0.10f && value > 0.08f
            val isYellowLeaf = hue in 35f..65f && sat > 0.15f && value > 0.20f
            val isBrownLeaf = hue in 5f..45f && sat > 0.10f && value > 0.10f
            val isPaleLeaf = sat < 0.25f && value > 0.20f && g >= (b * 0.9f)

            val isAnyLeafColor = isHealthyGreen || isYellowLeaf || isBrownLeaf || isPaleLeaf

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // BACKGROUND / HAND DETECTION
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            val isPureWhiteBg = r > 240 && g > 240 && b > 240 && confidence < 0.20f
            val isDarkSoil = value < 0.10f && confidence < 0.25f
            // Skin is tricky; only filter if confidence is low AND it doesn't look like a leaf
            val isSkin = (hue < 25f || hue > 335f) && sat in 0.20f..0.75f && value > 0.30f && r > g

            val shouldKeep = when {
                confidence > 0.60f -> true // High ML confidence
                isPureWhiteBg -> false
                isDarkSoil    -> false
                isSkin && !isAnyLeafColor && confidence < 0.40f -> false
                confidence > 0.25f && isAnyLeafColor -> true // Lower threshold for leaf colors
                isHealthyGreen && confidence > 0.05f -> true // Very aggressive for green
                isYellowLeaf   && confidence > 0.10f -> true
                isBrownLeaf    && confidence > 0.12f -> true
                else -> false
            }

            if (!shouldKeep) {
                pixels[i] = Color.WHITE
            }
        }

        val output = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return cleanIsolatedBlobs(output)
    }

    private fun cleanIsolatedBlobs(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height

        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val visited = java.util.BitSet(totalPixels)
        var maxCount = 0
        var bestStart = -1

        val queue = java.util.ArrayDeque<Int>()

        for (start in pixels.indices) {
            if (visited.get(start) || pixels[start] == Color.WHITE) continue

            var count = 0
            queue.add(start)
            visited.set(start)

            while (queue.isNotEmpty()) {
                val idx = queue.removeFirst()
                count++

                val x = idx % width
                val y = idx / width

                // 4-connectivity
                if (x > 0) {
                    val n = idx - 1
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
                if (x < width - 1) {
                    val n = idx + 1
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
                if (y > 0) {
                    val n = idx - width
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
                if (y < height - 1) {
                    val n = idx + width
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
            }

            if (count > maxCount) {
                maxCount = count
                bestStart = start
            }
        }

        // Only keep the largest component if it's significant
        if (bestStart != -1 && maxCount > totalPixels * 0.01) {
            val resultPixels = IntArray(totalPixels) { Color.WHITE }
            visited.clear()
            queue.add(bestStart)
            visited.set(bestStart)

            while (queue.isNotEmpty()) {
                val idx = queue.removeFirst()
                resultPixels[idx] = pixels[idx]

                val x = idx % width
                val y = idx / width

                if (x > 0) {
                    val n = idx - 1
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
                if (x < width - 1) {
                    val n = idx + 1
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
                if (y > 0) {
                    val n = idx - width
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
                if (y < height - 1) {
                    val n = idx + width
                    if (!visited.get(n) && pixels[n] != Color.WHITE) {
                        visited.set(n)
                        queue.add(n)
                    }
                }
            }
            val output = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            output.setPixels(resultPixels, 0, width, 0, 0, width, height)
            return output
        }

        return bitmap
    }

    private fun cropToLeaf(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        var minX = width; var minY = height
        var maxX = -1;    var maxY = -1

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height step 2) {
            for (x in 0 until width step 2) {
                if (pixels[y * width + x] != Color.WHITE) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        if (maxX <= minX || maxY <= minY) return bitmap

        // Add 5% padding
        val padW = ((maxX - minX) * 0.05).toInt()
        val padH = ((maxY - minY) * 0.05).toInt()

        val left   = (minX - padW).coerceAtLeast(0)
        val top    = (minY - padH).coerceAtLeast(0)
        val right  = (maxX + padW).coerceAtMost(width - 1)
        val bottom = (maxY + padH).coerceAtMost(height - 1)

        val cropWidth = right - left
        val cropHeight = bottom - top

        if (cropWidth <= 0 || cropHeight <= 0) return bitmap

        return try {
            Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }
}
