package com.assclk9000.app.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.assclk9000.app.data.model.ClickProfile
import com.assclk9000.app.service.ClickEngine
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtPurpleGlow
import com.assclk9000.app.ui.theme.CrtRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (profileId: Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val engineState by viewModel.engineState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ASSCLK.9000",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CrtPurple
                        )
                        Spacer(Modifier.width(12.dp))
                        EngineStatusIndicator(engineState = engineState)
                    }
                },
                actions = {
                    if (engineState == ClickEngine.EngineState.RUNNING ||
                        engineState == ClickEngine.EngineState.PAUSED
                    ) {
                        IconButton(onClick = { viewModel.stopExecution() }) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop execution",
                                tint = CrtRed
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(-1L) },
                containerColor = CrtPurple,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new profile"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (profiles.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        isRunning = engineState == ClickEngine.EngineState.RUNNING,
                        onPlay = { viewModel.startProfile(profile) },
                        onEdit = { onNavigateToEditor(profile.id) },
                        onDelete = { viewModel.deleteProfile(profile) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineStatusIndicator(engineState: ClickEngine.EngineState) {
    val infiniteTransition = rememberInfiniteTransition(label = "statusGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val statusColor by animateColorAsState(
        targetValue = when (engineState) {
            ClickEngine.EngineState.IDLE -> MaterialTheme.colorScheme.outline
            ClickEngine.EngineState.RUNNING -> CrtGreen
            ClickEngine.EngineState.PAUSED -> Color(0xFFFFBB33)
            ClickEngine.EngineState.STOPPING -> CrtRed
        },
        label = "statusColor"
    )

    val statusLabel = when (engineState) {
        ClickEngine.EngineState.IDLE -> "IDLE"
        ClickEngine.EngineState.RUNNING -> "RUNNING"
        ClickEngine.EngineState.PAUSED -> "PAUSED"
        ClickEngine.EngineState.STOPPING -> "STOPPING"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    if (engineState == ClickEngine.EngineState.RUNNING) {
                        statusColor.copy(alpha = glowAlpha)
                    } else {
                        statusColor
                    }
                )
                .then(
                    if (engineState == ClickEngine.EngineState.RUNNING) {
                        Modifier.shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = CrtGreen,
                            spotColor = CrtGreen
                        )
                    } else {
                        Modifier
                    }
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor
        )
    }
}

@Composable
private fun ProfileCard(
    profile: ClickProfile,
    isRunning: Boolean,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(CrtPurpleGlow, CrtPurple.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isRunning) CrtGreen else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (profile.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = profile.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run profile",
                            tint = CrtGreen
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit profile",
                            tint = CrtPurple
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete profile",
                            tint = CrtRed
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    label = if (profile.globalRepeatCount <= 0) "INF" else "${profile.globalRepeatCount}x",
                    color = MaterialTheme.colorScheme.primary
                )
                if (profile.globalIntervalMs > 0L) {
                    StatusChip(
                        label = formatMs(profile.globalIntervalMs),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (profile.isEnabled) {
                    StatusChip(label = "ENABLED", color = CrtGreen)
                } else {
                    StatusChip(label = "DISABLED", color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "> NO PROFILES FOUND_",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = CrtPurple
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap + to create your first click profile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatMs(millis: Long): String = when {
    millis >= 60_000L && millis % 60_000L == 0L -> "${millis / 60_000}m"
    millis >= 1_000L -> {
        val seconds = millis.toDouble() / 1_000
        if (seconds == seconds.toLong().toDouble()) "${seconds.toLong()}s"
        else "${seconds}s"
    }
    else -> "${millis}ms"
}
