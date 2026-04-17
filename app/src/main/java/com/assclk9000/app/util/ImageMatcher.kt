package com.assclk9000.app.util

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Bitmap comparison utility for template matching.
 * Uses pixel-by-pixel comparison with RGB distance and a sliding window
 * approach for locating a template within a larger screenshot.
 */
object ImageMatcher {

    /**
     * Result of a template match operation.
     *
     * @property x The X coordinate (top-left) where the best match was found.
     * @property y The Y coordinate (top-left) where the best match was found.
     * @property similarity Similarity score from 0.0 (no match) to 1.0 (perfect match).
     */
    data class MatchResult(
        val x: Int,
        val y: Int,
        val similarity: Float
    )

    /**
     * Attempts to find the template bitmap within the screenshot using a
     * sliding window with pixel-by-pixel RGB distance comparison.
     *
     * @param screenshot The full screenshot bitmap to search within.
     * @param template The template bitmap to search for.
     * @param threshold Minimum similarity (0.0 to 1.0) to consider a match. Default is 0.9.
     * @return A [MatchResult] if similarity >= threshold, or null if no match found.
     */
    fun matchTemplate(
        screenshot: Bitmap,
        template: Bitmap,
        threshold: Float = 0.9f
    ): MatchResult? {
        val sw = screenshot.width
        val sh = screenshot.height
        val tw = template.width
        val th = template.height

        if (tw > sw || th > sh) return null

        // Pre-extract template pixels for performance
        val templatePixels = IntArray(tw * th)
        template.getPixels(templatePixels, 0, tw, 0, 0, tw, th)

        var bestSimilarity = 0f
        var bestX = 0
        var bestY = 0

        // Sampling step for faster scanning (check every N-th pixel in initial pass)
        val step = maxOf(1, minOf(tw, th) / 4)

        for (y in 0..(sh - th)) {
            for (x in 0..(sw - tw)) {
                // Quick rejection: sample a few pixels first
                if (!quickCheck(screenshot, templatePixels, x, y, tw, th, step, threshold)) {
                    continue
                }

                val similarity = computeSimilarity(screenshot, templatePixels, x, y, tw, th)
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity
                    bestX = x
                    bestY = y
                }
            }
        }

        return if (bestSimilarity >= threshold) {
            MatchResult(bestX, bestY, bestSimilarity)
        } else {
            null
        }
    }

    /**
     * Performs a quick pixel sampling check to reject obviously non-matching positions early.
     */
    private fun quickCheck(
        screenshot: Bitmap,
        templatePixels: IntArray,
        offsetX: Int,
        offsetY: Int,
        tw: Int,
        th: Int,
        step: Int,
        threshold: Float
    ): Boolean {
        var totalDistance = 0.0
        var sampleCount = 0

        var ty = 0
        while (ty < th) {
            var tx = 0
            while (tx < tw) {
                val sp = screenshot.getPixel(offsetX + tx, offsetY + ty)
                val tp = templatePixels[ty * tw + tx]
                totalDistance += pixelDistance(sp, tp)
                sampleCount++
                tx += step
            }
            ty += step
        }

        if (sampleCount == 0) return true
        val avgDistance = totalDistance / sampleCount
        // Convert average distance to similarity (distance 0 = similarity 1.0)
        val similarity = 1.0f - (avgDistance / MAX_PIXEL_DISTANCE).toFloat()
        return similarity >= threshold - 0.1f // Allow slight margin for quick check
    }

    /**
     * Computes the full pixel-by-pixel similarity between the template and
     * the region of the screenshot starting at (offsetX, offsetY).
     */
    private fun computeSimilarity(
        screenshot: Bitmap,
        templatePixels: IntArray,
        offsetX: Int,
        offsetY: Int,
        tw: Int,
        th: Int
    ): Float {
        // Extract screenshot region pixels for bulk comparison
        val regionPixels = IntArray(tw * th)
        screenshot.getPixels(regionPixels, 0, tw, offsetX, offsetY, tw, th)

        var totalDistance = 0.0
        val pixelCount = tw * th

        for (i in 0 until pixelCount) {
            totalDistance += pixelDistance(regionPixels[i], templatePixels[i])
        }

        val avgDistance = totalDistance / pixelCount
        return 1.0f - (avgDistance / MAX_PIXEL_DISTANCE).toFloat()
    }

    /**
     * Compares two bitmaps of the same dimensions and returns a similarity score.
     *
     * @param a First bitmap.
     * @param b Second bitmap.
     * @return Similarity from 0.0 (completely different) to 1.0 (identical).
     */
    fun compareBitmaps(a: Bitmap, b: Bitmap): Float {
        if (a.width != b.width || a.height != b.height) {
            return 0f
        }

        val w = a.width
        val h = a.height
        val pixelsA = IntArray(w * h)
        val pixelsB = IntArray(w * h)
        a.getPixels(pixelsA, 0, w, 0, 0, w, h)
        b.getPixels(pixelsB, 0, w, 0, 0, w, h)

        var totalDistance = 0.0
        val pixelCount = w * h

        for (i in 0 until pixelCount) {
            totalDistance += pixelDistance(pixelsA[i], pixelsB[i])
        }

        val avgDistance = totalDistance / pixelCount
        return 1.0f - (avgDistance / MAX_PIXEL_DISTANCE).toFloat()
    }

    /**
     * Crops a rectangular region from the source bitmap.
     *
     * @param source Source bitmap to crop from.
     * @param x Left edge of the crop region.
     * @param y Top edge of the crop region.
     * @param width Width of the crop region.
     * @param height Height of the crop region.
     * @return A new bitmap containing the cropped region.
     */
    fun cropRegion(source: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
        val clampedX = x.coerceIn(0, source.width - 1)
        val clampedY = y.coerceIn(0, source.height - 1)
        val clampedW = width.coerceAtMost(source.width - clampedX)
        val clampedH = height.coerceAtMost(source.height - clampedY)
        return Bitmap.createBitmap(source, clampedX, clampedY, clampedW, clampedH)
    }

    /**
     * Computes the Euclidean distance between two ARGB pixel colors in RGB space.
     * Returns a value from 0.0 (identical) to [MAX_PIXEL_DISTANCE] (maximally different).
     */
    private fun pixelDistance(pixel1: Int, pixel2: Int): Double {
        val r1 = Color.red(pixel1)
        val g1 = Color.green(pixel1)
        val b1 = Color.blue(pixel1)
        val r2 = Color.red(pixel2)
        val g2 = Color.green(pixel2)
        val b2 = Color.blue(pixel2)

        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()

        return sqrt(dr * dr + dg * dg + db * db)
    }

    /** Maximum possible RGB distance: sqrt(255^2 + 255^2 + 255^2) */
    private const val MAX_PIXEL_DISTANCE = 441.6729559300637 // sqrt(3 * 255^2)
}
