package com.assclk9000.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.util.Log
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ClickProfile
import com.assclk9000.app.data.model.ConditionType
import com.assclk9000.app.data.model.GestureType
import com.assclk9000.app.data.repository.ProfileRepository
import com.assclk9000.app.util.GestureBuilder
import com.assclk9000.app.util.HumanSimulator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Orchestrates the execution of a [ClickProfile]'s action sequence.
 *
 * The engine runs each [ClickAction] in order, applying human simulation
 * (if configured), repeating actions per their repeat count, and respecting
 * the profile's global repeat count and interval settings.
 *
 * Communicates with [ClickerAccessibilityService] to dispatch gestures.
 */
@Singleton
class ClickEngine @Inject constructor(
    private val profileRepository: ProfileRepository
) {

    companion object {
        private const val TAG = "ClickEngine"
    }

    enum class EngineState {
        IDLE,
        RUNNING,
        PAUSED,
        STOPPING
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var executionJob: Job? = null

    private val _state = MutableStateFlow(EngineState.IDLE)
    val state: StateFlow<EngineState> = _state.asStateFlow()

    var currentProfile: ClickProfile? = null
        private set

    var currentActionIndex: Int = 0
        private set

    private var humanSimulator: HumanSimulator? = null

    /**
     * Starts executing the given profile's actions.
     *
     * @param profile The click profile to execute.
     * @param actions The ordered list of actions belonging to the profile.
     * @param humanIntensity The human simulation intensity (0.0 = disabled, 1.0 = full).
     */
    fun start(profile: ClickProfile, actions: List<ClickAction>, humanIntensity: Float = 0f) {
        if (_state.value == EngineState.RUNNING || _state.value == EngineState.PAUSED) {
            Log.w(TAG, "Engine already active, ignoring start()")
            return
        }

        if (actions.isEmpty()) {
            Log.w(TAG, "No actions to execute")
            return
        }

        currentProfile = profile
        humanSimulator = if (humanIntensity > 0f) HumanSimulator(humanIntensity) else null
        _state.value = EngineState.RUNNING

        executionJob = scope.launch {
            try {
                executeProfile(profile, actions)
            } catch (e: CancellationException) {
                Log.d(TAG, "Execution cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Execution error", e)
            } finally {
                _state.value = EngineState.IDLE
                currentProfile = null
                currentActionIndex = 0
            }
        }
    }

    /**
     * Pauses the currently running execution. Actions in progress will complete,
     * but the next action will not begin until [resume] is called.
     */
    fun pause() {
        if (_state.value == EngineState.RUNNING) {
            _state.value = EngineState.PAUSED
            Log.d(TAG, "Paused")
        }
    }

    /**
     * Resumes a paused execution.
     */
    fun resume() {
        if (_state.value == EngineState.PAUSED) {
            _state.value = EngineState.RUNNING
            Log.d(TAG, "Resumed")
        }
    }

    /**
     * Stops the current execution. The engine transitions through STOPPING
     * and settles at IDLE once the execution coroutine exits.
     */
    fun stop() {
        if (_state.value == EngineState.IDLE) return
        _state.value = EngineState.STOPPING
        executionJob?.cancel()
        executionJob = null
        Log.d(TAG, "Stopped")
    }

    /**
     * Executes the full profile: iterates through all actions for each global repeat cycle.
     */
    private suspend fun executeProfile(profile: ClickProfile, actions: List<ClickAction>) {
        val simulator = humanSimulator

        // Apply natural start delay if human simulation is active
        if (simulator != null) {
            val startDelay = simulator.addNaturalStartDelay()
            if (startDelay > 0) {
                delay(startDelay)
            }
        }

        // Handle initial delay from schedule config
        if (profile.scheduleConfig.delayBeforeStart > 0L) {
            delay(profile.scheduleConfig.delayBeforeStart)
        }

        val globalRepeats = if (profile.globalRepeatCount <= 0) Int.MAX_VALUE else profile.globalRepeatCount
        val stopAfterMs = profile.scheduleConfig.stopAfterMs
        val stopAfterRepeats = profile.scheduleConfig.stopAfterRepeats
        val startTime = System.currentTimeMillis()

        var globalIteration = 0

        while (globalIteration < globalRepeats) {
            // Check time-based stop condition
            if (stopAfterMs > 0L && (System.currentTimeMillis() - startTime) >= stopAfterMs) {
                Log.d(TAG, "Stop-after-time reached")
                break
            }

            // Check repeat-based stop condition from schedule
            if (stopAfterRepeats > 0 && globalIteration >= stopAfterRepeats) {
                Log.d(TAG, "Stop-after-repeats reached")
                break
            }

            // Execute each action in order
            for ((index, action) in actions.withIndex()) {
                currentActionIndex = index
                checkStateOrWait()

                if (_state.value == EngineState.STOPPING) return

                executeAction(action)

                // Check time-based stop after each action
                if (stopAfterMs > 0L && (System.currentTimeMillis() - startTime) >= stopAfterMs) {
                    Log.d(TAG, "Stop-after-time reached mid-cycle")
                    return
                }
            }

            globalIteration++

            // Global interval between full cycles
            if (globalIteration < globalRepeats && profile.globalIntervalMs > 0L) {
                val interval = if (simulator != null) {
                    simulator.gaussianJitter(profile.globalIntervalMs)
                } else {
                    profile.globalIntervalMs
                }
                delay(interval)
            }
        }

        Log.d(TAG, "Profile execution completed ($globalIteration cycles)")
    }

    /**
     * Executes a single [ClickAction], including its per-action repeat count,
     * condition checks, interval delays, and human simulation effects.
     */
    private suspend fun executeAction(action: ClickAction) {
        val simulator = humanSimulator

        // Handle WAIT gesture type — just delay
        if (action.gestureType == GestureType.WAIT) {
            val waitTime = if (simulator != null) {
                simulator.gaussianJitter(action.duration)
            } else {
                action.duration
            }
            delay(waitTime)
            return
        }

        // Evaluate pre-conditions
        if (action.condition != null) {
            when (action.condition.type) {
                ConditionType.NONE -> { /* proceed normally */ }
                ConditionType.WAIT_FOR_IMAGE -> {
                    // The caller (OverlayService / ImageDetectionService) should handle
                    // image wait conditions externally. For now, log and proceed.
                    Log.d(TAG, "WAIT_FOR_IMAGE condition — requires ImageDetectionService")
                }
                ConditionType.CLICK_ON_IMAGE -> {
                    Log.d(TAG, "CLICK_ON_IMAGE condition — requires ImageDetectionService")
                }
                ConditionType.STOP_IF_IMAGE_GONE -> {
                    Log.d(TAG, "STOP_IF_IMAGE_GONE condition — requires ImageDetectionService")
                }
            }
        }

        val repeats = if (action.repeatCount <= 0) 1 else action.repeatCount

        for (rep in 0 until repeats) {
            checkStateOrWait()
            if (_state.value == EngineState.STOPPING) return

            // Human simulation: occasional micro-pause before action
            if (simulator != null && simulator.shouldMicroPause()) {
                val pauseMs = simulator.microPauseDuration()
                Log.v(TAG, "Micro-pause: ${pauseMs}ms")
                delay(pauseMs)
            }

            // Build gesture with optional human simulation
            val gesture = GestureBuilder.buildFromAction(action, simulator)

            // Dispatch gesture via the accessibility service
            dispatchGestureAndWait(gesture)

            // Inter-repeat interval
            if (rep < repeats - 1 || action.intervalAfter > 0L) {
                var intervalMs = action.intervalAfter

                // Apply per-action jitter
                if (action.jitterMs > 0L) {
                    val jitter = (-action.jitterMs..action.jitterMs).random()
                    intervalMs = (intervalMs + jitter).coerceAtLeast(1L)
                }

                // Apply human simulation jitter on top
                if (simulator != null) {
                    intervalMs = simulator.gaussianJitter(intervalMs)
                }

                if (intervalMs > 0L) {
                    delay(intervalMs)
                }
            }
        }
    }

    /**
     * Dispatches a [GestureDescription] through the accessibility service and
     * suspends until the gesture completes or is cancelled.
     *
     * @return true if the gesture completed successfully, false if cancelled or no service.
     */
    private suspend fun dispatchGestureAndWait(gesture: GestureDescription): Boolean {
        val service = ClickerAccessibilityService.instance
        if (service == null) {
            Log.e(TAG, "Accessibility service not available")
            stop()
            return false
        }

        return suspendCancellableCoroutine { continuation ->
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Log.w(TAG, "Gesture cancelled by system")
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            }

            val dispatched = service.dispatchGesture(gesture, callback)
            if (!dispatched) {
                Log.w(TAG, "Failed to dispatch gesture")
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
    }

    /**
     * Checks the current state and suspends if paused.
     * Polls every 100ms while paused.
     */
    private suspend fun checkStateOrWait() {
        while (_state.value == EngineState.PAUSED) {
            delay(100L)
        }
    }
}
