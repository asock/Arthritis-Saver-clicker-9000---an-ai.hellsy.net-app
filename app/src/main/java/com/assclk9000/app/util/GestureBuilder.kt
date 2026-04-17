package com.assclk9000.app.util

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.GestureType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Builds Android [GestureDescription] objects from [ClickAction] models.
 * Used by the accessibility service to dispatch gestures.
 */
object GestureBuilder {

    /**
     * Builds a simple tap gesture at the specified coordinates.
     */
    fun buildTap(x: Float, y: Float, duration: Long): GestureDescription {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        return GestureDescription.Builder()
            .addStroke(stroke)
            .build()
    }

    /**
     * Builds a long press gesture at the specified coordinates.
     * Identical to a tap but with a longer duration.
     */
    fun buildLongPress(x: Float, y: Float, duration: Long): GestureDescription {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        return GestureDescription.Builder()
            .addStroke(stroke)
            .build()
    }

    /**
     * Builds a straight-line swipe gesture between two points.
     */
    fun buildSwipe(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        duration: Long
    ): GestureDescription {
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        return GestureDescription.Builder()
            .addStroke(stroke)
            .build()
    }

    /**
     * Builds a curved swipe gesture using quadratic/cubic Bezier curves
     * for more natural-looking movement.
     *
     * @param points List of (x, y) points defining the path. Must contain at least 2 points.
     * @param duration Total gesture duration in milliseconds.
     */
    fun buildBezierSwipe(
        points: List<Pair<Float, Float>>,
        duration: Long
    ): GestureDescription {
        require(points.size >= 2) { "Bezier swipe requires at least 2 points" }

        val path = Path().apply {
            moveTo(points[0].first, points[0].second)

            when (points.size) {
                2 -> {
                    lineTo(points[1].first, points[1].second)
                }
                3 -> {
                    quadTo(
                        points[1].first, points[1].second,
                        points[2].first, points[2].second
                    )
                }
                else -> {
                    // For 4+ points, chain cubic Bezier segments
                    var i = 1
                    while (i < points.size - 2) {
                        if (i + 2 < points.size) {
                            cubicTo(
                                points[i].first, points[i].second,
                                points[i + 1].first, points[i + 1].second,
                                points[i + 2].first, points[i + 2].second
                            )
                            i += 3
                        } else {
                            quadTo(
                                points[i].first, points[i].second,
                                points[i + 1].first, points[i + 1].second
                            )
                            i += 2
                        }
                    }
                    // Connect remaining points with lines if any
                    while (i < points.size) {
                        lineTo(points[i].first, points[i].second)
                        i++
                    }
                }
            }
        }

        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        return GestureDescription.Builder()
            .addStroke(stroke)
            .build()
    }

    /**
     * Builds a pinch gesture using two simultaneous strokes moving
     * symmetrically toward or away from the center point.
     *
     * @param centerX Center X of the pinch.
     * @param centerY Center Y of the pinch.
     * @param startDistance Initial distance from center for each finger.
     * @param endDistance Final distance from center for each finger.
     * @param duration Gesture duration in milliseconds.
     */
    fun buildPinch(
        centerX: Float,
        centerY: Float,
        startDistance: Float,
        endDistance: Float,
        duration: Long
    ): GestureDescription {
        // Finger 1: moves along 45-degree angle (top-left to center area)
        val angle1 = Math.toRadians(135.0)
        // Finger 2: moves along opposite 45-degree angle (bottom-right to center area)
        val angle2 = Math.toRadians(-45.0)

        val path1 = Path().apply {
            moveTo(
                centerX + (startDistance * cos(angle1)).toFloat(),
                centerY + (startDistance * sin(angle1)).toFloat()
            )
            lineTo(
                centerX + (endDistance * cos(angle1)).toFloat(),
                centerY + (endDistance * sin(angle1)).toFloat()
            )
        }

        val path2 = Path().apply {
            moveTo(
                centerX + (startDistance * cos(angle2)).toFloat(),
                centerY + (startDistance * sin(angle2)).toFloat()
            )
            lineTo(
                centerX + (endDistance * cos(angle2)).toFloat(),
                centerY + (endDistance * sin(angle2)).toFloat()
            )
        }

        val stroke1 = GestureDescription.StrokeDescription(path1, 0L, duration)
        val stroke2 = GestureDescription.StrokeDescription(path2, 0L, duration)

        return GestureDescription.Builder()
            .addStroke(stroke1)
            .addStroke(stroke2)
            .build()
    }

    /**
     * Main entry point that builds a [GestureDescription] from a [ClickAction],
     * optionally applying human simulation for more natural gesture execution.
     *
     * @param action The click action to build a gesture for.
     * @param humanSimulator Optional [HumanSimulator] for adding human-like imperfections.
     */
    fun buildFromAction(
        action: ClickAction,
        humanSimulator: HumanSimulator? = null
    ): GestureDescription {
        val (x, y) = if (humanSimulator != null) {
            humanSimulator.driftCoordinate(action.x, action.y)
        } else {
            action.x to action.y
        }

        val duration = if (humanSimulator != null) {
            humanSimulator.humanizedDuration(action.duration)
        } else {
            action.duration
        }

        return when (action.gestureType) {
            GestureType.TAP -> buildTap(x, y, duration)

            GestureType.LONG_PRESS -> buildLongPress(x, y, duration)

            GestureType.SWIPE -> {
                val (x2, y2) = if (humanSimulator != null && action.x2 != null && action.y2 != null) {
                    humanSimulator.driftCoordinate(action.x2, action.y2)
                } else {
                    (action.x2 ?: x) to (action.y2 ?: y)
                }

                if (humanSimulator != null) {
                    val swipePath = Path().apply {
                        moveTo(x, y)
                        lineTo(x2, y2)
                    }
                    val wobbled = humanSimulator.wobblePath(swipePath)
                    val stroke = GestureDescription.StrokeDescription(wobbled, 0L, duration)
                    GestureDescription.Builder()
                        .addStroke(stroke)
                        .build()
                } else {
                    buildSwipe(x, y, x2, y2, duration)
                }
            }

            GestureType.PINCH -> {
                val startDist = action.x2 ?: 200f
                val endDist = action.y2 ?: 50f
                buildPinch(x, y, startDist, endDist, duration)
            }

            GestureType.CUSTOM_PATH -> {
                // Custom path defaults to a tap if no additional data
                buildTap(x, y, duration)
            }

            GestureType.WAIT -> {
                // WAIT doesn't produce a real gesture; build a no-op tap at 0,0 with 1ms
                // The service layer should handle WAIT by delaying instead of dispatching.
                buildTap(0f, 0f, 1L)
            }
        }
    }
}
