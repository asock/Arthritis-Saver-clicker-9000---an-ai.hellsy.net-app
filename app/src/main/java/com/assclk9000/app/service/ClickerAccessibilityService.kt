package com.assclk9000.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Core accessibility service that dispatches gesture commands on behalf of the app.
 * Appears in system settings as "System Input Service" (stealth naming).
 *
 * This service does not process accessibility events — it exists solely to
 * leverage [AccessibilityService.dispatchGesture] for programmatic input.
 */
class ClickerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "InputService"

        /** Stealth intent action for internal binding/communication. */
        const val ACTION_INPUT_SERVICE = "com.assclk9000.app.INPUT_SERVICE"

        /**
         * Static reference to the running service instance.
         * Null when the service is not connected.
         */
        @Volatile
        var instance: ClickerAccessibilityService? = null
            private set

        /** Whether the accessibility service is currently connected and available. */
        val isRunning: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally empty — we do not process accessibility events.
        // This service is used exclusively for gesture dispatch.
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        Log.d(TAG, "Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    /**
     * Dispatches a single tap/click gesture at the given screen coordinates.
     *
     * @param x The X coordinate in screen pixels.
     * @param y The Y coordinate in screen pixels.
     * @param duration The duration of the gesture in milliseconds.
     * @param callback Optional callback for gesture completion/cancellation.
     */
    fun dispatchClick(
        x: Float,
        y: Float,
        duration: Long = 50L,
        callback: GestureResultCallback? = null
    ) {
        val path = android.graphics.Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        dispatchGesture(gesture, callback ?: DefaultGestureCallback(), null)
    }

    /**
     * Dispatches an arbitrary [GestureDescription] built externally (e.g., by [GestureBuilder]).
     *
     * @param gestureDescription The gesture to dispatch.
     * @param callback Optional callback for gesture completion/cancellation.
     * @return true if the gesture was successfully dispatched, false otherwise.
     */
    fun dispatchGesture(
        gestureDescription: GestureDescription,
        callback: GestureResultCallback? = null
    ): Boolean {
        return dispatchGesture(gestureDescription, callback ?: DefaultGestureCallback(), null)
    }

    /**
     * Default callback that logs gesture results for debugging.
     */
    private class DefaultGestureCallback : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            Log.v(TAG, "Gesture completed")
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            Log.w(TAG, "Gesture cancelled")
        }
    }
}
