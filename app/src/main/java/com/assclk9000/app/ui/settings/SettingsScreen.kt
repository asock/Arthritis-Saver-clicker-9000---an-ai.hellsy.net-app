package com.assclk9000.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val theme by viewModel.theme.collectAsState()
    val defaultInterval by viewModel.defaultInterval.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val overlayOpacity by viewModel.overlayOpacity.collectAsState()
    val overlaySize by viewModel.overlaySize.collectAsState()
    val discreteMode by viewModel.discreteMode.collectAsState()
    val genericNotifications by viewModel.genericNotifications.collectAsState()
    val humanSimulationIntensity by viewModel.humanSimulationIntensity.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Appearance ──────────────────────────────────────────
            item {
                SectionHeader("Appearance")
            }

            item {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Dark", "Light", "System").forEach { option ->
                        FilterChip(
                            selected = theme == option,
                            onClick = { viewModel.setTheme(option) },
                            label = { Text(option) }
                        )
                    }
                }
            }

            item {
                SliderSetting(
                    label = "Overlay Opacity",
                    value = overlayOpacity,
                    onValueChange = { viewModel.setOverlayOpacity(it) },
                    valueRange = 0f..1f,
                    displayValue = "${(overlayOpacity * 100).toInt()}%"
                )
            }

            item {
                SliderSetting(
                    label = "Overlay Size",
                    value = overlaySize,
                    onValueChange = { viewModel.setOverlaySize(it) },
                    valueRange = 0.5f..2.0f,
                    displayValue = "${(overlaySize * 100).toInt()}%"
                )
            }

            // ── Behavior ────────────────────────────────────────────
            item {
                SectionHeader("Behavior")
            }

            item {
                Text(
                    text = "Default Interval (ms)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = defaultInterval.toString(),
                    onValueChange = { text ->
                        text.toLongOrNull()?.let { viewModel.setDefaultInterval(it) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SwitchSetting(
                    label = "Haptic Feedback",
                    checked = hapticFeedback,
                    onCheckedChange = { viewModel.setHapticFeedback(it) }
                )
            }

            // ── Discrete Mode ───────────────────────────────────────
            item {
                SectionHeader("Discrete Mode")
            }

            item {
                SwitchSetting(
                    label = "Discrete Mode",
                    description = "Hide floating controls, use notification only",
                    checked = discreteMode,
                    onCheckedChange = { viewModel.setDiscreteMode(it) }
                )
            }

            item {
                SwitchSetting(
                    label = "Generic Notifications",
                    description = "Use system-style notification text",
                    checked = genericNotifications,
                    onCheckedChange = { viewModel.setGenericNotifications(it) }
                )
            }

            item {
                SliderSetting(
                    label = "Human Simulation",
                    description = "Adds natural variation to timing and position",
                    value = humanSimulationIntensity,
                    onValueChange = { viewModel.setHumanSimulationIntensity(it) },
                    valueRange = 0f..1f,
                    displayValue = "${(humanSimulationIntensity * 100).toInt()}%"
                )
            }

            // ── Data ────────────────────────────────────────────────
            item {
                SectionHeader("Data")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* TODO: export profiles */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export All Profiles")
                    }
                    Button(
                        onClick = { /* TODO: import profiles */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import Profiles")
                    }
                }
            }

            // ── About ───────────────────────────────────────────────
            item {
                SectionHeader("About")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Arthritis Saver Clicker 9000",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Made with arthritis in mind",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    description: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
