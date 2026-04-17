package com.assclk9000.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ClickProfile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that provides a floating overlay with play/pause/stop controls.
 *
 * STEALTH: The notification channel is named "Accessibility Service" and the
 * notification title is "Input Accessibility Active" — no clicker keywords.
 *
 * Supports "discrete mode" where no floating overlay is shown; controls are
 * available only through notification action buttons.
 */
@AndroidEntryPoint
class OverlayService : Service() {

    companion object {
        private const val TAG = "OverlayService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "input_accessibility_channel"

        const val EXTRA_PROFILE_ID = "extra_profile_id"
        const val EXTRA_DISCRETE_MODE = "extra_discrete_mode"
        const val EXTRA_HUMAN_INTENSITY = "extra_human_intensity"

        const val ACTION_PLAY = "com.assclk9000.app.ACTION_PLAY"
        const val ACTION_PAUSE = "com.assclk9000.app.ACTION_PAUSE"
        const val ACTION_STOP = "com.assclk9000.app.ACTION_STOP"

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    @Inject
    lateinit var clickEngine: ClickEngine

    @Inject
    lateinit var profileRepository: com.assclk9000.app.data.repository.ProfileRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var discreteMode: Boolean = false
    private var humanIntensity: Float = 0f

    private var currentProfile: ClickProfile? = null
    private var currentActions: List<ClickAction> = emptyList()

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
            ACTION_STOP -> handleStop()
            else -> {
                // Initial start — load profile and set up overlay
                val profileId = intent?.getLongExtra(EXTRA_PROFILE_ID, -1L) ?: -1L
                discreteMode = intent?.getBooleanExtra(EXTRA_DISCRETE_MODE, false) ?: false
                humanIntensity = intent?.getFloatExtra(EXTRA_HUMAN_INTENSITY, 0f) ?: 0f

                startForeground(NOTIFICATION_ID, buildNotification(isPaused = false))

                if (profileId > 0L) {
                    loadProfileAndStart(profileId)
                }

                if (!discreteMode) {
                    showOverlay()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeOverlay()
        clickEngine.stop()
        serviceScope.cancel()
        isRunning = false
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    // ── Profile Loading ─────────────────────────────────────────────────

    private fun loadProfileAndStart(profileId: Long) {
        serviceScope.launch {
            try {
                val profile = profileRepository.getProfileById(profileId).first()
                val actions = profileRepository.getActionsForProfile(profileId).first()

                if (profile != null && actions.isNotEmpty()) {
                    currentProfile = profile
                    currentActions = actions
                    clickEngine.start(profile, actions, humanIntensity)
                    updateNotification(isPaused = false)
                } else {
                    Log.w(TAG, "Profile $profileId not found or has no actions")
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load profile", e)
                stopSelf()
            }
        }
    }

    // ── Control Handlers ────────────────────────────────────────────────

    private fun handlePlay() {
        val state = clickEngine.state.value
        when (state) {
            ClickEngine.EngineState.PAUSED -> {
                clickEngine.resume()
                updateNotification(isPaused = false)
            }
            ClickEngine.EngineState.IDLE -> {
                // Re-start if we have a loaded profile
                val profile = currentProfile
                if (profile != null && currentActions.isNotEmpty()) {
                    clickEngine.start(profile, currentActions, humanIntensity)
                    updateNotification(isPaused = false)
                }
            }
            else -> { /* already running or stopping */ }
        }
    }

    private fun handlePause() {
        clickEngine.pause()
        updateNotification(isPaused = true)
    }

    private fun handleStop() {
        clickEngine.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Notification ────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(com.assclk9000.app.R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(com.assclk9000.app.R.string.notification_channel_description)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(isPaused: Boolean): Notification {
        val playPauseAction = if (isPaused) {
            val playIntent = Intent(this, OverlayService::class.java).apply {
                action = ACTION_PLAY
            }
            val playPending = PendingIntent.getService(
                this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Notification.Action.Builder(
                android.R.drawable.ic_media_play,
                getString(com.assclk9000.app.R.string.action_play),
                playPending
            ).build()
        } else {
            val pauseIntent = Intent(this, OverlayService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePending = PendingIntent.getService(
                this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Notification.Action.Builder(
                android.R.drawable.ic_media_pause,
                getString(com.assclk9000.app.R.string.action_pause),
                pausePending
            ).build()
        }

        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = Notification.Action.Builder(
            android.R.drawable.ic_delete,
            getString(com.assclk9000.app.R.string.action_stop),
            stopPending
        ).build()

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setSmallIcon(android.R.drawable.ic_menu_preferences)
            .setContentTitle(getString(com.assclk9000.app.R.string.notification_title))
            .setContentText(getString(com.assclk9000.app.R.string.notification_text))
            .setOngoing(true)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .build()
    }

    private fun updateNotification(isPaused: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(isPaused))
    }

    // ── Floating Overlay ────────────────────────────────────────────────

    private fun showOverlay() {
        if (overlayView != null) return

        val composeView = ComposeView(this).apply {
            setContent {
                OverlayContent(
                    clickEngine = clickEngine,
                    onPlay = { handlePlay() },
                    onPause = { handlePause() },
                    onStop = { handleStop() }
                )
            }
        }

        // Set up lifecycle for ComposeView
        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager?.addView(composeView, params)
        overlayView = composeView

        Log.d(TAG, "Overlay shown")
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing overlay view", e)
            }
        }
        overlayView = null
    }

    // ── Lifecycle Owner for ComposeView ─────────────────────────────────

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        init {
            savedStateRegistryController.performRestore(null)
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }
}

// ── Overlay Composable ──────────────────────────────────────────────────

@Composable
private fun OverlayContent(
    clickEngine: ClickEngine,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    val engineState by clickEngine.state.collectAsState()

    Column(
        modifier = Modifier
            .background(
                color = Color(0xDD1E1E2E),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = when (engineState) {
                ClickEngine.EngineState.IDLE -> "Ready"
                ClickEngine.EngineState.RUNNING -> "Running"
                ClickEngine.EngineState.PAUSED -> "Paused"
                ClickEngine.EngineState.STOPPING -> "Stopping"
            },
            color = Color.White,
            fontSize = 10.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause toggle
            IconButton(
                onClick = {
                    when (engineState) {
                        ClickEngine.EngineState.RUNNING -> onPause()
                        ClickEngine.EngineState.PAUSED,
                        ClickEngine.EngineState.IDLE -> onPlay()
                        else -> { /* stopping, ignore */ }
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (engineState == ClickEngine.EngineState.RUNNING)
                            android.R.drawable.ic_media_pause
                        else
                            android.R.drawable.ic_media_play
                    ),
                    contentDescription = if (engineState == ClickEngine.EngineState.RUNNING) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Stop button
            IconButton(
                onClick = onStop,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_delete),
                    contentDescription = "Stop",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
