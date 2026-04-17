package com.assclk9000.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ConditionType
import com.assclk9000.app.data.model.GestureType
import com.assclk9000.app.ui.theme.CrtAmber
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtMagenta
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtRed

// ============================================================================
// ActionCard -- displays a single ClickAction with controls
// ============================================================================

/**
 * A CRT-themed card that displays a [ClickAction] with its gesture type,
 * coordinates, timing, jitter indicator, condition badge, and action buttons.
 *
 * @param action The [ClickAction] to display.
 * @param onEdit Callback when the edit button is tapped.
 * @param onDelete Callback when the delete button is tapped.
 * @param modifier Modifier applied to the root Card.
 * @param showDragHandle Whether to show the drag handle for reordering.
 */
@Composable
fun ActionCard(
    action: ClickAction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Drag handle ---
            if (showDragHandle) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

            // --- Gesture icon ---
            GestureIcon(
                gestureType = action.gestureType,
                modifier = Modifier.size(36.dp)
            )

            Spacer(Modifier.width(12.dp))

            // --- Details column ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Gesture name
                Text(
                    text = gestureDisplayName(action.gestureType),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Coordinates
                Text(
                    text = formatCoordinates(action),
                    style = MaterialTheme.typography.bodySmall,
                    color = CrtPurple
                )

                // Interval + Duration row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "every ${formatMillis(action.intervalAfter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "hold ${formatMillis(action.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (action.repeatCount > 1) {
                        Text(
                            text = "x${action.repeatCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrtMagenta
                        )
                    }
                }

                // Badges row (jitter, condition)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (action.jitterMs > 0 || action.jitterPx > 0f) {
                        Badge(
                            text = "JITTER",
                            color = CrtAmber
                        )
                    }
                    val conditionType = action.condition?.type
                    if (conditionType != null && conditionType != ConditionType.NONE) {
                        Badge(
                            text = conditionBadgeText(conditionType),
                            color = CrtGreen
                        )
                    }
                }

                // Description if set
                if (action.description.isNotBlank()) {
                    Text(
                        text = action.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // --- Action buttons ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit action",
                        tint = CrtPurple
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete action",
                        tint = CrtRed
                    )
                }
            }
        }
    }
}

// ============================================================================
// GestureIcon -- renders the appropriate icon for a gesture type
// ============================================================================

@Composable
private fun GestureIcon(
    gestureType: GestureType,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (gestureType) {
        GestureType.TAP -> Icons.Default.TouchApp to CrtPurple
        GestureType.LONG_PRESS -> Icons.Default.PanTool to CrtMagenta
        GestureType.SWIPE -> Icons.Default.NorthEast to CrtAmber
        GestureType.PINCH -> Icons.Default.OpenWith to CrtAmber
        GestureType.CUSTOM_PATH -> Icons.Default.Gesture to CrtMagenta
        GestureType.WAIT -> Icons.Default.Timer to CrtGreen
    }

    Icon(
        imageVector = icon,
        contentDescription = gestureDisplayName(gestureType),
        tint = tint,
        modifier = modifier
    )
}

// ============================================================================
// Badge -- small inline label chip
// ============================================================================

@Composable
private fun Badge(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// ============================================================================
// Formatting helpers
// ============================================================================

private fun gestureDisplayName(type: GestureType): String = when (type) {
    GestureType.TAP -> "Tap"
    GestureType.LONG_PRESS -> "Long Press"
    GestureType.SWIPE -> "Swipe"
    GestureType.PINCH -> "Pinch"
    GestureType.CUSTOM_PATH -> "Custom Path"
    GestureType.WAIT -> "Wait"
}

private fun formatCoordinates(action: ClickAction): String {
    val primary = "(${action.x.toInt()}, ${action.y.toInt()})"
    return if (action.x2 != null && action.y2 != null) {
        "$primary -> (${action.x2.toInt()}, ${action.y2.toInt()})"
    } else {
        primary
    }
}

private fun formatMillis(millis: Long): String = when {
    millis >= 60_000L && millis % 60_000L == 0L -> "${millis / 60_000}m"
    millis >= 1_000L -> {
        val seconds = millis.toDouble() / 1_000
        if (seconds == seconds.toLong().toDouble()) "${seconds.toLong()}s"
        else "${seconds}s"
    }
    else -> "${millis}ms"
}

private fun conditionBadgeText(type: ConditionType): String = when (type) {
    ConditionType.NONE -> ""
    ConditionType.WAIT_FOR_IMAGE -> "WAIT IMG"
    ConditionType.CLICK_ON_IMAGE -> "CLICK IMG"
    ConditionType.STOP_IF_IMAGE_GONE -> "STOP IMG"
}
