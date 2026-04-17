package com.assclk9000.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.core.view.ViewCompat

/**
 * Converts a duration in milliseconds to a human-readable format.
 * Examples: "500ms", "1.5s", "2m 30s"
 */
fun Long.toFormattedDuration(): String {
    return when {
        this < 1000L -> "${this}ms"
        this < 60_000L -> {
            val seconds = this / 1000.0
            if (seconds == seconds.toLong().toDouble()) {
                "${seconds.toLong()}s"
            } else {
                "${"%.1f".format(seconds)}s"
            }
        }
        else -> {
            val minutes = this / 60_000L
            val remainingSeconds = (this % 60_000L) / 1000L
            if (remainingSeconds > 0) {
                "${minutes}m ${remainingSeconds}s"
            } else {
                "${minutes}m"
            }
        }
    }
}

/**
 * Converts a value in pixels to density-independent pixels (dp).
 */
fun Float.toDp(context: Context): Float {
    return this / context.resources.displayMetrics.density
}

/**
 * Converts a value in density-independent pixels (dp) to pixels.
 */
fun Float.toPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}

/**
 * Triggers a vibration for the specified duration.
 * Uses VibratorManager on API 31+ and falls back to the legacy Vibrator service.
 */
fun Context.vibrate(durationMs: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    }
}

/**
 * Sets the accessibility content description on a View using ViewCompat.
 */
fun View.setAccessibilityDescription(desc: String) {
    ViewCompat.setAccessibilityDelegate(this, null)
    contentDescription = desc
}
