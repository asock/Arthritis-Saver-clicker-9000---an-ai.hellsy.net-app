package com.assclk9000.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Picker mode controlling what kind of coordinate selection the overlay supports.
 */
enum class PickerMode {
    /** Single-point tap selection. */
    TAP,
    /** Two-point swipe selection (start + end). */
    SWIPE
}

/**
 * Full-screen composable overlay for picking coordinates on screen.
 *
 * In [PickerMode.TAP] mode a crosshair follows the user's finger and a single
 * coordinate pair is captured. In [PickerMode.SWIPE] mode the user drags from
 * a start point to an end point and both coordinate pairs are captured.
 *
 * @param mode Whether the picker captures a single tap or a swipe path.
 * @param onTapSelected Called with (x, y) when a single coordinate is confirmed.
 * @param onSwipeSelected Called with (x1, y1, x2, y2) when a swipe is confirmed.
 * @param onCancel Called when the user cancels the picker.
 */
@Composable
fun TargetPickerOverlay(
    mode: PickerMode,
    onTapSelected: (Float, Float) -> Unit,
    onSwipeSelected: (Float, Float, Float, Float) -> Unit,
    onCancel: () -> Unit
) {
    // Touch state
    var touchX by remember { mutableFloatStateOf(-1f) }
    var touchY by remember { mutableFloatStateOf(-1f) }
    var endX by remember { mutableFloatStateOf(-1f) }
    var endY by remember { mutableFloatStateOf(-1f) }
    var hasTouched by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    // CRT green for the crosshair
    val crosshairColor = Color(0xFF33FF33)
    // Subtle grid color
    val gridColor = crosshairColor.copy(alpha = 0.15f)
    // Swipe path line color
    val pathColor = Color(0xFFBB33FF)

    // Monospace text style for coordinate readout
    val monoStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .then(
                when (mode) {
                    PickerMode.SWIPE -> Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchX = offset.x
                                touchY = offset.y
                                endX = offset.x
                                endY = offset.y
                                hasTouched = true
                                isDragging = true
                            },
                            onDrag = { change, _ ->
                                endX = change.position.x
                                endY = change.position.y
                            },
                            onDragEnd = {
                                isDragging = false
                            }
                        )
                    }

                    PickerMode.TAP -> Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                touchX = offset.x
                                touchY = offset.y
                                hasTouched = true
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                touchX = change.position.x
                                touchY = change.position.y
                                hasTouched = true
                            }
                        }
                }
            )
    ) {
        // ── Grid + crosshair canvas ────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 80f

            // Vertical grid lines
            var gx = 0f
            while (gx < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, size.height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                gx += gridSpacing
            }

            // Horizontal grid lines
            var gy = 0f
            while (gy < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gy),
                    end = Offset(size.width, gy),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                gy += gridSpacing
            }

            // Crosshair at touch point
            if (hasTouched) {
                // Vertical crosshair line
                drawLine(
                    color = crosshairColor,
                    start = Offset(touchX, 0f),
                    end = Offset(touchX, size.height),
                    strokeWidth = 1.5f
                )
                // Horizontal crosshair line
                drawLine(
                    color = crosshairColor,
                    start = Offset(0f, touchY),
                    end = Offset(size.width, touchY),
                    strokeWidth = 1.5f
                )
                // Center circle
                drawCircle(
                    color = crosshairColor,
                    radius = 16f,
                    center = Offset(touchX, touchY),
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = crosshairColor,
                    radius = 4f,
                    center = Offset(touchX, touchY)
                )

                // SWIPE mode: draw path from start to current/end touch
                if (mode == PickerMode.SWIPE && endX >= 0f && endY >= 0f) {
                    // End-point crosshair circle
                    drawCircle(
                        color = pathColor,
                        radius = 16f,
                        center = Offset(endX, endY),
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = pathColor,
                        radius = 4f,
                        center = Offset(endX, endY)
                    )

                    // Path line
                    drawLine(
                        color = pathColor,
                        start = Offset(touchX, touchY),
                        end = Offset(endX, endY),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                    )

                    // Arrow head at end
                    val arrowSize = 20f
                    val angle = kotlin.math.atan2(
                        (endY - touchY).toDouble(),
                        (endX - touchX).toDouble()
                    ).toFloat()
                    val arrowP1 = Offset(
                        endX - arrowSize * kotlin.math.cos(angle - 0.5f).toFloat(),
                        endY - arrowSize * kotlin.math.sin(angle - 0.5f).toFloat()
                    )
                    val arrowP2 = Offset(
                        endX - arrowSize * kotlin.math.cos(angle + 0.5f).toFloat(),
                        endY - arrowSize * kotlin.math.sin(angle + 0.5f).toFloat()
                    )
                    drawLine(
                        color = pathColor,
                        start = Offset(endX, endY),
                        end = arrowP1,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = pathColor,
                        start = Offset(endX, endY),
                        end = arrowP2,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // ── Coordinate text readout near the touch point ───────────
        if (hasTouched) {
            val readoutOffsetX = if (touchX > 500f) -200 else 24
            val readoutOffsetY = if (touchY > 200f) -80 else 24

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (touchX + readoutOffsetX).roundToInt(),
                            (touchY + readoutOffsetY).roundToInt()
                        )
                    }
                    .background(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = "X: ${touchX.toInt()}  Y: ${touchY.toInt()}",
                        style = monoStyle,
                        color = crosshairColor
                    )
                    if (mode == PickerMode.SWIPE && endX >= 0f) {
                        Text(
                            text = "-> X: ${endX.toInt()}  Y: ${endY.toInt()}",
                            style = monoStyle,
                            color = pathColor
                        )
                    }
                }
            }
        }

        // ── Confirm / Cancel FABs at the bottom ───────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            // Cancel FAB
            FloatingActionButton(
                onClick = onCancel,
                containerColor = Color.Black.copy(alpha = 0.85f),
                contentColor = Color(0xFFFF3333),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Confirm FAB
            FloatingActionButton(
                onClick = {
                    if (hasTouched) {
                        when (mode) {
                            PickerMode.SWIPE -> onSwipeSelected(touchX, touchY, endX, endY)
                            PickerMode.TAP -> onTapSelected(touchX, touchY)
                        }
                    }
                },
                containerColor = if (hasTouched) {
                    Color(0xFF33FF33)
                } else {
                    Color(0xFF33FF33).copy(alpha = 0.3f)
                },
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
