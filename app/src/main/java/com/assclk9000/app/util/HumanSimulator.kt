package com.assclk9000.app.util

import android.graphics.Path
import android.graphics.PathMeasure
import kotlin.math.abs
import kotlin.math.max

/**
 * Makes automated gestures appear human-like by adding Gaussian-distributed
 * jitter, micro-drift, duration variation, hesitation pauses, and path wobble.
 *
 * All effects scale linearly with [intensity] (0.0 = no effect, 1.0 = full effect).
 *
 * @param intensity Controls the magnitude of all human simulation effects (0.0 to 1.0).
 */
class HumanSimulator(
    val intensity: Float
) {

    private val kotlinRandom = kotlin.random.Random
    private val javaRandom = java.util.Random()

    /**
     * Adds Gaussian-distributed timing jitter to a base interval.
     *
     * At intensity 1.0, the standard deviation is 15% of [baseMs].
     * The result is clamped to be positive (minimum 1ms).
     *
     * @param baseMs The base time interval in milliseconds.
     * @return The jittered interval, always >= 1.
     */
    fun gaussianJitter(baseMs: Long): Long {
        if (intensity <= 0f || baseMs <= 0L) return baseMs
        val stdDev = baseMs * 0.15 * intensity
        val jittered = baseMs + (javaRandom.nextGaussian() * stdDev).toLong()
        return max(1L, jittered)
    }

    /**
     * Adds micro-drift to coordinates using Gaussian distribution.
     *
     * At intensity 1.0, the maximum drift is ~3px (Gaussian with stdDev = 1.0px,
     * so most drifts are well under 3px but occasional ones reach it).
     *
     * @param x The original X coordinate.
     * @param y The original Y coordinate.
     * @return A pair of drifted (X, Y) coordinates.
     */
    fun driftCoordinate(x: Float, y: Float): Pair<Float, Float> {
        if (intensity <= 0f) return x to y
        val maxDrift = 3.0 * intensity
        val stdDev = maxDrift / 3.0 // ~99.7% of values within maxDrift
        val dx = (javaRandom.nextGaussian() * stdDev).toFloat()
        val dy = (javaRandom.nextGaussian() * stdDev).toFloat()
        return (x + dx) to (y + dy)
    }

    /**
     * Varies touch hold duration using Gaussian distribution.
     *
     * At intensity 1.0, applies +/-20% variation (stdDev = 10% of base,
     * so ~95% of results are within +/-20%).
     * The result is clamped to be positive (minimum 1ms).
     *
     * @param baseMs The base touch duration in milliseconds.
     * @return The humanized duration, always >= 1.
     */
    fun humanizedDuration(baseMs: Long): Long {
        if (intensity <= 0f || baseMs <= 0L) return baseMs
        val variationFraction = 0.20 * intensity
        val stdDev = baseMs * variationFraction / 2.0 // 2-sigma = variationFraction
        val varied = baseMs + (javaRandom.nextGaussian() * stdDev).toLong()
        return max(1L, varied)
    }

    /**
     * Simulates human hesitation by randomly deciding whether to insert a micro-pause.
     *
     * At intensity 1.0, returns true approximately 5% of the time.
     * Probability scales linearly with intensity.
     *
     * @return true if a micro-pause should be inserted.
     */
    fun shouldMicroPause(): Boolean {
        if (intensity <= 0f) return false
        val probability = 0.05 * intensity
        return kotlinRandom.nextDouble() < probability
    }

    /**
     * Returns a Gaussian-distributed micro-pause duration between 200ms and 800ms.
     *
     * The distribution is centered at 500ms with stdDev of 100ms,
     * clamped to the [200, 800] range.
     *
     * @return Pause duration in milliseconds.
     */
    fun microPauseDuration(): Long {
        val mean = 500.0
        val stdDev = 100.0
        val duration = mean + javaRandom.nextGaussian() * stdDev
        return duration.toLong().coerceIn(200L, 800L)
    }

    /**
     * Takes a straight-line path and adds subtle wobble to make it look hand-drawn.
     *
     * The original path is measured, then re-interpolated into [numPoints] segments.
     * At each intermediate point, perpendicular Gaussian noise is added, scaled
     * by [intensity]. The amount of wobble peaks in the middle of the stroke and
     * tapers near the start/end (like a real finger movement).
     *
     * @param path The original straight-line (or any) path to wobble.
     * @param numPoints Number of interpolation points (more = smoother wobble).
     * @return A new Path with subtle human-like wobble applied.
     */
    fun wobblePath(path: Path, numPoints: Int = 10): Path {
        if (intensity <= 0f || numPoints < 3) return path

        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length

        if (pathLength <= 0f) return path

        val wobbledPath = Path()
        val pos = FloatArray(2)
        val tan = FloatArray(2)

        for (i in 0..numPoints) {
            val distance = (i.toFloat() / numPoints) * pathLength
            pathMeasure.getPosTan(distance, pos, tan)

            if (i == 0) {
                wobbledPath.moveTo(pos[0], pos[1])
            } else if (i == numPoints) {
                // End point: no wobble, land precisely
                wobbledPath.lineTo(pos[0], pos[1])
            } else {
                // Perpendicular direction: rotate tangent by 90 degrees
                val perpX = -tan[1]
                val perpY = tan[0]

                // Taper wobble near start and end
                val taper = 1.0f - abs(2.0f * i / numPoints - 1.0f)
                val maxWobble = 2.0 * intensity * taper
                val wobbleAmount = (javaRandom.nextGaussian() * maxWobble / 3.0).toFloat()

                val wx = pos[0] + perpX * wobbleAmount
                val wy = pos[1] + perpY * wobbleAmount

                wobbledPath.lineTo(wx, wy)
            }
        }

        return wobbledPath
    }

    /**
     * Simulates the natural delay before a human begins an action.
     * Humans don't start clicking instantly after deciding to click.
     *
     * Returns a Gaussian-distributed delay between 50ms and 300ms,
     * centered at 175ms.
     *
     * @return Initial delay in milliseconds.
     */
    fun addNaturalStartDelay(): Long {
        if (intensity <= 0f) return 0L
        val mean = 175.0 * intensity
        val stdDev = 40.0 * intensity
        val delay = mean + javaRandom.nextGaussian() * stdDev
        return delay.toLong().coerceIn(50L, 300L)
    }
}
