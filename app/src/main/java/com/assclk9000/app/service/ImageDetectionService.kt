package com.assclk9000.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.assclk9000.app.data.model.ActionCondition
import com.assclk9000.app.util.ImageMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service that captures the screen via MediaProjection and performs template-matching
 * image detection for conditional click actions.
 *
 * Lifecycle:
 * 1. Started with MediaProjection result data via [onStartCommand].
 * 2. Sets up [VirtualDisplay] + [ImageReader] for screenshot capture.
 * 3. Callers invoke [startDetection] with an [ActionCondition] and receive callbacks.
 * 4. [stopDetection] halts the periodic check loop.
 * 5. [onDestroy] cleans up all projection resources.
 */
@AndroidEntryPoint
class ImageDetectionService : Service() {

    companion object {
        private const val TAG = "ImageDetection"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "image_detection_channel"
        private const val VIRTUAL_DISPLAY_NAME = "ScreenCapture"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        const val EXTRA_CHECK_INTERVAL_MS = "extra_check_interval_ms"

        /** Default interval between screenshot captures for detection. */
        const val DEFAULT_CHECK_INTERVAL_MS = 500L

        @Volatile
        var instance: ImageDetectionService? = null
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var detectionJob: Job? = null

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var screenDensity: Int = 0
    private var checkIntervalMs: Long = DEFAULT_CHECK_INTERVAL_MS

    private var latestBitmap: Bitmap? = null
    private val bitmapLock = Object()

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        obtainScreenMetrics()
        Log.d(TAG, "Service created (${screenWidth}x${screenHeight} @ ${screenDensity}dpi)")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        checkIntervalMs = intent?.getLongExtra(EXTRA_CHECK_INTERVAL_MS, DEFAULT_CHECK_INTERVAL_MS)
            ?: DEFAULT_CHECK_INTERVAL_MS

        if (resultCode == -1 || resultData == null) {
            Log.e(TAG, "Invalid MediaProjection result data")
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        setupMediaProjection(resultCode, resultData)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopDetection()
        releaseProjection()
        serviceScope.cancel()
        instance = null
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    // ── Screen Metrics ──────────────────────────────────────────────────

    private fun obtainScreenMetrics() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
            val configuration = resources.configuration
            screenDensity = configuration.densityDpi
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            screenDensity = metrics.densityDpi
        }
    }

    // ── MediaProjection Setup ───────────────────────────────────────────

    private fun setupMediaProjection(resultCode: Int, resultData: Intent) {
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData).also { mp ->
            mp.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection stopped by system")
                    releaseProjection()
                }
            }, handler)
        }

        setupVirtualDisplay()
    }

    private fun setupVirtualDisplay() {
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2 // maxImages — double buffer
        ).also { reader ->
            reader.setOnImageAvailableListener({ ir ->
                processImage(ir)
            }, handler)
        }

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null,
            handler
        )

        Log.d(TAG, "VirtualDisplay created")
    }

    /**
     * Called when a new frame is available from the ImageReader.
     * Converts the latest frame to a Bitmap for detection use.
     */
    private fun processImage(reader: ImageReader) {
        var image: Image? = null
        try {
            image = reader.acquireLatestImage() ?: return

            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth

            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            // Crop to actual screen size if there's row padding
            val cropped = if (rowPadding > 0) {
                Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight).also {
                    bitmap.recycle()
                }
            } else {
                bitmap
            }

            synchronized(bitmapLock) {
                latestBitmap?.recycle()
                latestBitmap = cropped
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        } finally {
            image?.close()
        }
    }

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Captures the current screen and returns a snapshot Bitmap.
     * Returns null if no frame has been captured yet.
     */
    fun captureScreen(): Bitmap? {
        synchronized(bitmapLock) {
            return latestBitmap?.copy(Bitmap.Config.ARGB_8888, false)
        }
    }

    /**
     * Starts periodic image detection against the given condition.
     *
     * The callback is invoked each cycle with either a [ImageMatcher.MatchResult]
     * (if the template was found with sufficient similarity) or null (if not found).
     *
     * @param condition The action condition containing the template image and search region.
     * @param callback Invoked on each detection cycle with the match result.
     */
    fun startDetection(
        condition: ActionCondition,
        callback: (ImageMatcher.MatchResult?) -> Unit
    ) {
        stopDetection()

        val templateData = condition.imageData
        if (templateData == null || templateData.isEmpty()) {
            Log.w(TAG, "No template image data in condition")
            return
        }

        val templateBitmap = android.graphics.BitmapFactory.decodeByteArray(
            templateData, 0, templateData.size
        )
        if (templateBitmap == null) {
            Log.e(TAG, "Failed to decode template bitmap")
            return
        }

        detectionJob = serviceScope.launch {
            Log.d(TAG, "Detection started (interval=${checkIntervalMs}ms)")

            while (isActive) {
                val screenshot = captureScreen()
                if (screenshot != null) {
                    val searchBitmap = if (condition.regionWidth > 0 && condition.regionHeight > 0) {
                        // Crop to search region for faster matching
                        ImageMatcher.cropRegion(
                            screenshot,
                            condition.regionX,
                            condition.regionY,
                            condition.regionWidth,
                            condition.regionHeight
                        ).also { screenshot.recycle() }
                    } else {
                        screenshot
                    }

                    val result = ImageMatcher.matchTemplate(
                        searchBitmap,
                        templateBitmap,
                        condition.similarityThreshold
                    )

                    // Adjust coordinates back to full-screen if we searched a sub-region
                    val adjustedResult = if (result != null &&
                        condition.regionWidth > 0 && condition.regionHeight > 0
                    ) {
                        ImageMatcher.MatchResult(
                            x = result.x + condition.regionX,
                            y = result.y + condition.regionY,
                            similarity = result.similarity
                        )
                    } else {
                        result
                    }

                    searchBitmap.recycle()
                    callback(adjustedResult)
                } else {
                    callback(null)
                }

                delay(checkIntervalMs)
            }

            templateBitmap.recycle()
            Log.d(TAG, "Detection stopped")
        }
    }

    /**
     * Stops the current detection loop, if any.
     */
    fun stopDetection() {
        detectionJob?.cancel()
        detectionJob = null
    }

    // ── Cleanup ─────────────────────────────────────────────────────────

    private fun releaseProjection() {
        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.close()
        imageReader = null

        mediaProjection?.stop()
        mediaProjection = null

        synchronized(bitmapLock) {
            latestBitmap?.recycle()
            latestBitmap = null
        }

        Log.d(TAG, "Projection resources released")
    }

    // ── Notification ────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Analysis",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background screen analysis service"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("System Service Active")
            .setContentText("Analyzing input patterns")
            .setOngoing(true)
            .build()
    }
}
