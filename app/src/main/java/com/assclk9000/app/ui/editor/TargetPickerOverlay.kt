package com.assclk9000.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtPurpleGlow
import com.assclk9000.app.ui.theme.CrtRed
import kotlin.math.roundToInt

/**
 * Full-screen transparent overlay composable for picking coordinates.
 *
 * In [isSwipeMode], the user touches to set a start point and drags to
 * set an end point, drawing a path between the two.
 *
 * Otherwise, a single tap/touch sets the target coordinate with a crosshair.
 *
 * @param isSwipeMode Whether the picker captures a start and end point (for swipes).
 * @param onCoordinateSelected Callback when a single coordinate is confirmed.
 * @param onSwipeSelected Callback when a swipe start/end coordinate pair is confirmed.
 * @param onCancel Callback when the user cancels the picker.
 */
@Composable
fun TargetPickerOverlay(
    isSwipeMode: Boolean = false,
    onCoordinateSelected: (Float, Float) -> Unit = { _, _ -> },
    onSwipeSelected: (Float, Float, Float, Float) -> Unit = { _, _, _, _ -> },
    onCancel: () -> Unit = {}
) {
    var touchX by remember { mutableFloatStateOf(-1f) }
    var touchY by remember { mutableFloatStateOf(-1f) }
    var endX by remember { mutableFloatStateOf(-1f) }
    var endY by remember { mutableFloatStateOf(-1f) }
    var hasTouched by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val gridColor = CrtPurpleGlow.copy(alpha = 0.15f)
    val crosshairColor = CrtGreen
    val pathColor = CrtPurple

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .then(
                if (isSwipeMode) {
                    Modifier.pointerInput(Unit) {
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
                } else {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchX = offset.x
                            touchY = offset.y
                            hasTouched = true
                        }
                    }.pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            touchX = change.position.x
                            touchY = change.position.y
                            hasTouched = true
                        }
                    }
                }
            )
    ) {
        // Grid overlay
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

                // Swipe mode: draw path from start to end
                if (isSwipeMode && endX >= 0f && endY >= 0f) {
                    // End crosshair
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

        // Coordinate readout near touch point
        if (hasTouched) {
            val readoutOffsetX = if (touchX > 500f) -180 else 20
            val readoutOffsetY = if (touchY > 200f) -80 else 20

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
                        style = MaterialTheme.typography.labelMedium,
                        color = crosshairColor
                    )
                    if (isSwipeMode && endX >= 0f) {
                        Text(
                            text = "-> X: ${endX.toInt()}  Y: ${endY.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = pathColor
                        )
                    }
                }
            }
        }

        // Confirm / Cancel floating buttons at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CrtRed,
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                onClick = {
                    if (hasTouched) {
                        if (isSwipeMode) {
                            onSwipeSelected(touchX, touchY, endX, endY)
                        } else {
                            onCoordinateSelected(touchX, touchY)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
                enabled = hasTouched,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrtGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = CrtGreen.copy(alpha = 0.3f),
                    disabledContentColor = Color.Black.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
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
