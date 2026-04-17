package com.assclk9000.app.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assclk9000.app.service.ClickEngine

private val CrtPurple = Color(0xFFBB33FF)
private val CrtGreen = Color(0xFF33FF33)
private val CrtAmber = Color(0xFFFFBB33)
private val CrtSurface = Color(0xFF1A1A2E)
private val CrtOnSurface = Color(0xFFCCCCFF)

@Composable
fun FloatingControlPanel(
    engineState: ClickEngine.EngineState,
    currentActionDescription: String,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onMinimize: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val bubbleColor by animateColorAsState(
        targetValue = when (engineState) {
            ClickEngine.EngineState.RUNNING -> CrtGreen
            ClickEngine.EngineState.PAUSED -> CrtAmber
            else -> CrtPurple
        },
        animationSpec = tween(300),
        label = "bubbleColor"
    )

    if (!isExpanded) {
        MinimizedBubble(
            color = bubbleColor,
            isRunning = engineState == ClickEngine.EngineState.RUNNING,
            onClick = { isExpanded = true }
        )
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        ExpandedPanel(
            engineState = engineState,
            currentActionDescription = currentActionDescription,
            bubbleColor = bubbleColor,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onMinimize = {
                isExpanded = false
                onMinimize()
            }
        )
    }
}

@Composable
private fun MinimizedBubble(
    color: Color,
    isRunning: Boolean,
    onClick: () -> Unit
) {
    val pulseAlpha = if (isRunning) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val alpha by transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        alpha
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .alpha(pulseAlpha)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Expand controls",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ExpandedPanel(
    engineState: ClickEngine.EngineState,
    currentActionDescription: String,
    bubbleColor: Color,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onMinimize: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CrtSurface)
            .border(1.dp, CrtPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        val statusText = when (engineState) {
            ClickEngine.EngineState.RUNNING -> "Running"
            ClickEngine.EngineState.PAUSED -> "Paused"
            ClickEngine.EngineState.STOPPING -> "Stopping"
            ClickEngine.EngineState.IDLE -> "Idle"
        }

        Text(
            text = statusText,
            color = bubbleColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        if (currentActionDescription.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentActionDescription,
                color = CrtOnSurface,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.width(200.dp)
        ) {
            IconButton(
                onClick = {
                    when (engineState) {
                        ClickEngine.EngineState.RUNNING -> onPause()
                        else -> onPlay()
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (engineState == ClickEngine.EngineState.RUNNING)
                        Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (engineState == ClickEngine.EngineState.RUNNING)
                        "Pause" else "Play",
                    tint = CrtGreen
                )
            }

            IconButton(
                onClick = onStop,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop",
                    tint = Color(0xFFFF3333)
                )
            }

            IconButton(
                onClick = onMinimize,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Minimize",
                    tint = CrtOnSurface
                )
            }
        }
    }
}
