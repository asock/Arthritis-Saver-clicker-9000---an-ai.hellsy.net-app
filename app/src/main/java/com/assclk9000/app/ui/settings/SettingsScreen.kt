package com.assclk9000.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.assclk9000.app.ui.components.IntervalPicker
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val theme by viewModel.theme.collectAsState()
    val overlayOpacity by viewModel.overlayOpacity.collectAsState()
    val overlaySize by viewModel.overlaySize.collectAsState()
    val defaultInterval by viewModel.defaultInterval.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val discreteMode by viewModel.discreteMode.collectAsState()
    val genericNotifications by viewModel.genericNotifications.collectAsState()
    val humanSimulation by viewModel.humanSimulationIntensity.collectAsState()

    // SAF launchers for export/import
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportAllProfiles(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importProfiles(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "> SETTINGS_",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = CrtPurple
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ================================================================
            // APPEARANCE
            // ================================================================
            SectionHeader(title = "APPEARANCE")

            // Theme selector
            SettingLabel(text = "Theme")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("dark", "light", "system").forEach { option ->
                    FilterChip(
                        selected = theme == option,
                        onClick = { viewModel.setTheme(option) },
                        label = {
                            Text(
                                text = option.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrtPurple,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Overlay opacity slider
            SliderSetting(
                label = "Overlay Opacity",
                value = overlayOpacity,
                valueLabel = "${(overlayOpacity * 100).toInt()}%",
                onValueChange = { viewModel.setOverlayOpacity(it) },
                valueRange = 0.1f..1.0f
            )

            // Overlay size slider
            SliderSetting(
                label = "Overlay Size",
                value = overlaySize,
                valueLabel = "${(overlaySize * 100).toInt()}%",
                onValueChange = { viewModel.setOverlaySize(it) },
                valueRange = 0.5f..2.0f
            )

            SectionDivider()

            // ================================================================
            // BEHAVIOR
            // ================================================================
            SectionHeader(title = "BEHAVIOR")

            // Default interval picker
            IntervalPicker(
                valueMillis = defaultInterval,
                onValueChange = { viewModel.setDefaultInterval(it) },
                label = "Default Interval"
            )

            Spacer(Modifier.height(4.dp))

            // Haptic feedback toggle
            ToggleSetting(
                label = "Haptic Feedback",
                description = "Vibrate on action execution",
                checked = hapticFeedback,
                onCheckedChange = { viewModel.setHapticFeedback(it) }
            )

            SectionDivider()

            // ================================================================
            // STEALTH (Discrete Mode)
            // ================================================================
            SectionHeader(title = "DISCRETE MODE")

            // Discrete mode toggle
            ToggleSetting(
                label = "Discrete Mode",
                description = "Hide floating controls, use notification only",
                checked = discreteMode,
                onCheckedChange = { viewModel.setDiscreteMode(it) }
            )

            // Generic notifications toggle
            ToggleSetting(
                label = "Generic Notifications",
                description = "Use system-style notification text",
                checked = genericNotifications,
                onCheckedChange = { viewModel.setGenericNotifications(it) }
            )

            // Human simulation slider
            SliderSetting(
                label = "Human Simulation",
                value = humanSimulation,
                valueLabel = "${(humanSimulation * 100).toInt()}%",
                description = "Adds natural variation to timing and position",
                onValueChange = { viewModel.setHumanSimulationIntensity(it) },
                valueRange = 0f..1f
            )

            SectionDivider()

            // ================================================================
            // DATA
            // ================================================================
            SectionHeader(title = "DATA")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { exportLauncher.launch("assclk9000_profiles.json") },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrtPurple,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Export All",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CrtPurple
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Import",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            SectionDivider()

            // ================================================================
            // ABOUT
            // ================================================================
            SectionHeader(title = "ABOUT")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrtSurfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AboutRow(label = "App", value = "Arthritis Saver Clicker 9000")
                AboutRow(label = "Version", value = "1.0.0")
                AboutRow(label = "Package", value = "com.assclk9000.app")
                AboutRow(
                    label = "Accessibility",
                    value = "Uses AccessibilityService for gesture dispatch"
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Made with arthritis in mind",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrtPurple
                )
            }

            // Bottom spacing
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ============================================================================
// Section Header
// ============================================================================

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = "> $title",
        style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = FontFamily.Monospace
        ),
        color = CrtPurple,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// ============================================================================
// Setting Label
// ============================================================================

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// ============================================================================
// Toggle Setting
// ============================================================================

@Composable
private fun ToggleSetting(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
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
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CrtGreen,
                checkedTrackColor = CrtGreen.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ============================================================================
// Slider Setting
// ============================================================================

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = CrtPurple
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = CrtPurple,
                activeTrackColor = CrtPurple,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
        )
    }
}

// ============================================================================
// About Row
// ============================================================================

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
