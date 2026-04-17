package com.assclk9000.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtOutline
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtPurpleGlow
import com.assclk9000.app.ui.theme.CrtSurfaceVariant

// ============================================================================
// CoordinateDisplay -- shows x,y with a visual mini-preview
// ============================================================================

/**
 * Displays screen coordinates with a miniature preview showing the
 * approximate dot position. Tapping opens the target picker overlay.
 *
 * @param x The X coordinate in screen pixels.
 * @param y The Y coordinate in screen pixels.
 * @param screenWidth The device screen width in pixels (used for preview scaling).
 * @param screenHeight The device screen height in pixels (used for preview scaling).
 * @param onClick Callback when the component is tapped (opens target picker).
 * @param modifier Modifier applied to the root layout.
 * @param label Optional label displayed above the coordinate text.
 */
@Composable
fun CoordinateDisplay(
    x: Float,
    y: Float,
    screenWidth: Int = 1080,
    screenHeight: Int = 1920,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Target Position"
) {
    Column(modifier = modifier) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CrtSurfaceVariant)
                .border(
                    width = 1.dp,
                    color = CrtOutline,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    role = Role.Button,
                    onClickLabel = "Pick target coordinates",
                    onClick = onClick
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Mini preview box ---
            MiniPreview(
                x = x,
                y = y,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                modifier = Modifier.size(width = 48.dp, height = 80.dp)
            )

            Spacer(Modifier.width(16.dp))

            // --- Coordinate text ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CoordinateLabel(axis = "X", value = x.toInt())
                    CoordinateLabel(axis = "Y", value = y.toInt())
                }

                Text(
                    text = "Tap to pick new target",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- Crosshair icon ---
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Pick target",
                tint = CrtPurple,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// CoordinateLabel -- displays "X: 540" in monospace
// ============================================================================

@Composable
private fun CoordinateLabel(
    axis: String,
    value: Int
) {
    Row {
        Text(
            text = "$axis: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            color = CrtPurple
        )
    }
}

// ============================================================================
// MiniPreview -- tiny screen rectangle with a dot at the target position
// ============================================================================

@Composable
private fun MiniPreview(
    x: Float,
    y: Float,
    screenWidth: Int,
    screenHeight: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = CrtOutline,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            // Calculate dot position scaled to canvas
            val dotX = if (screenWidth > 0) {
                (x / screenWidth) * size.width
            } else {
                size.width / 2
            }
            val dotY = if (screenHeight > 0) {
                (y / screenHeight) * size.height
            } else {
                size.height / 2
            }

            // Clamp to canvas bounds
            val clampedX = dotX.coerceIn(0f, size.width)
            val clampedY = dotY.coerceIn(0f, size.height)

            // Glow ring
            drawCircle(
                color = CrtPurpleGlow,
                radius = 8f,
                center = Offset(clampedX, clampedY)
            )

            // Solid dot
            drawCircle(
                color = CrtGreen,
                radius = 4f,
                center = Offset(clampedX, clampedY)
            )

            // Crosshair lines
            drawLine(
                color = CrtGreen.copy(alpha = 0.3f),
                start = Offset(clampedX, 0f),
                end = Offset(clampedX, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = CrtGreen.copy(alpha = 0.3f),
                start = Offset(0f, clampedY),
                end = Offset(size.width, clampedY),
                strokeWidth = 1f
            )
        }
    }
}
