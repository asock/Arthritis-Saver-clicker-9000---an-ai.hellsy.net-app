package com.assclk9000.app.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.assclk9000.app.service.ClickEngine
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtPurpleGlow
import com.assclk9000.app.ui.theme.CrtRed
import kotlinx.coroutines.flow.StateFlow

/**
 * Floating control panel composable for use in the overlay service.
 *
 * Displays a small CRT-purple bubble when minimized. When tapped, expands
 * to show Play/Pause, Stop, current action info, and a minimize button.
 *
 * The panel is draggable: touch offset is remembered and the caller receives
 * drag deltas via [onDragDelta] to update WindowManager LayoutParams.
 *
 * @param engineStateFlow The click engine state flow to observe.
 * @param currentActionInfo Description of the currently executing action.
 * @param onPlay Called when the play button is tapped.
 * @param onPause Called when the pause button is tapped.
 * @param onStop Called when the stop button is tapped.
 * @param onDragDelta Called with (dx, dy) pixel offsets when the panel is dragged.
 */
@Composable
fun FloatingControlPanel(
    engineStateFlow: StateFlow<ClickEngine.EngineState>,
    currentActionInfo: String = "",
    onPlay: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onDragDelta: (Float, Float) -> Unit = { _, _ -> }
) {
    val engineState by engineStateFlow.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    // Glow animation for running state
    val infiniteTransition = rememberInfiniteTransition(label = "panelGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val isRunning = engineState == ClickEngine.EngineState.RUNNING

    // Draggable wrapper
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        // Minimized bubble
        AnimatedVisibility(
            visible = !isExpanded,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .then(
                        if (isRunning) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = CrtGreen.copy(alpha = glowAlpha),
                                spotColor = CrtGreen.copy(alpha = glowAlpha)
                            )
                        } else {
                            Modifier.shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = CrtPurpleGlow,
                                spotColor = CrtPurpleGlow
                            )
                        }
                    )
                    .clip(CircleShape)
                    .background(
                        if (isRunning) CrtGreen.copy(alpha = 0.9f)
                        else CrtPurple.copy(alpha = 0.9f)
                    )
                    .border(
                        width = 2.dp,
                        color = if (isRunning) CrtGreen else CrtPurple,
                        shape = CircleShape
                    )
                    .clickable(role = Role.Button) { isExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (engineState) {
                        ClickEngine.EngineState.RUNNING -> "||"
                        ClickEngine.EngineState.PAUSED -> ">"
                        else -> "9K"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.Black
                )
            }
        }

        // Expanded panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .then(
                        if (isRunning) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = CrtGreen.copy(alpha = glowAlpha),
                                spotColor = CrtGreen.copy(alpha = glowAlpha)
                            )
                        } else {
                            Modifier.shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = CrtPurpleGlow,
                                spotColor = CrtPurpleGlow
                            )
                        }
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xDD1A1A2E))
                    .border(
                        width = 1.dp,
                        color = if (isRunning) CrtGreen.copy(alpha = 0.6f)
                        else CrtPurple.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.width(160.dp)
                ) {
                    Text(
                        text = "ASSCLK.9000",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = CrtPurple
                    )
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (engineState) {
                                    ClickEngine.EngineState.RUNNING -> CrtGreen.copy(
                                        alpha = glowAlpha
                                    )
                                    ClickEngine.EngineState.PAUSED -> Color(0xFFFFBB33)
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            )
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Engine state label
                Text(
                    text = when (engineState) {
                        ClickEngine.EngineState.IDLE -> "IDLE"
                        ClickEngine.EngineState.RUNNING -> "RUNNING"
                        ClickEngine.EngineState.PAUSED -> "PAUSED"
                        ClickEngine.EngineState.STOPPING -> "STOPPING..."
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = when (engineState) {
                        ClickEngine.EngineState.RUNNING -> CrtGreen
                        ClickEngine.EngineState.PAUSED -> Color(0xFFFFBB33)
                        ClickEngine.EngineState.STOPPING -> CrtRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                // Current action info
                if (currentActionInfo.isNotBlank()) {
                    Text(
                        text = currentActionInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Control buttons row
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
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRunning) CrtGreen.copy(alpha = 0.2f)
                                else CrtPurple.copy(alpha = 0.2f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause
                            else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Play",
                            tint = if (isRunning) CrtGreen else CrtPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Stop button
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(CircleShape)
                            .background(CrtRed.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = CrtRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Minimize button
                    IconButton(
                        onClick = { isExpanded = false },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Minimize",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
