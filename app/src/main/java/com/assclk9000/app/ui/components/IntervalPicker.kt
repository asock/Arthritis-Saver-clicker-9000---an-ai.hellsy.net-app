package com.assclk9000.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// ============================================================================
// Time unit used by the picker
// ============================================================================
enum class TimeUnit(val label: String, val toMillis: Long) {
    MILLISECONDS("ms", 1L),
    SECONDS("s", 1_000L),
    MINUTES("m", 60_000L);
}

// ============================================================================
// Preset quick-pick buttons
// ============================================================================
private data class Preset(val label: String, val millis: Long)

private val PRESETS = listOf(
    Preset("100ms", 100L),
    Preset("500ms", 500L),
    Preset("1s", 1_000L),
    Preset("5s", 5_000L)
)

// ============================================================================
// IntervalPicker composable
// ============================================================================

/**
 * A reusable time-interval input combining a numeric text field, a unit
 * selector (ms / s / m), and preset quick-pick buttons.
 *
 * @param valueMillis Current interval value in milliseconds.
 * @param onValueChange Called with the new value in milliseconds whenever the
 *   user changes the numeric input, switches units, or taps a preset.
 * @param label Optional label displayed above the text field.
 * @param modifier Modifier applied to the root layout.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntervalPicker(
    valueMillis: Long,
    onValueChange: (Long) -> Unit,
    label: String = "Interval",
    modifier: Modifier = Modifier
) {
    // --- internal state: which unit is selected + the raw text ---
    var selectedUnit by remember { mutableStateOf(bestUnitFor(valueMillis)) }
    var textValue by remember(valueMillis, selectedUnit) {
        mutableStateOf(formatForUnit(valueMillis, selectedUnit))
    }

    Column(modifier = modifier.fillMaxWidth()) {

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(4.dp))

        // --- Text field + unit chips row ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { raw ->
                    textValue = raw
                    val parsed = raw.toDoubleOrNull()
                    if (parsed != null && parsed >= 0) {
                        onValueChange((parsed * selectedUnit.toMillis).toLong())
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
            )

            Spacer(Modifier.width(8.dp))

            // Unit selector chips
            TimeUnit.entries.forEach { unit ->
                FilterChip(
                    selected = selectedUnit == unit,
                    onClick = {
                        selectedUnit = unit
                        // Re-compute text for the new unit, keeping same millis value
                        val currentMillis = textValue.toDoubleOrNull()
                            ?.let { (it * selectedUnit.toMillis).toLong() }
                            ?: valueMillis
                        textValue = formatForUnit(currentMillis, unit)
                        // Don't re-emit onValueChange -- millis hasn't changed
                    },
                    label = {
                        Text(
                            text = unit.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .padding(horizontal = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Formatted display ---
        Text(
            text = formatDisplay(valueMillis),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // --- Preset quick buttons ---
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PRESETS.forEach { preset ->
                AssistChip(
                    onClick = {
                        onValueChange(preset.millis)
                        selectedUnit = bestUnitFor(preset.millis)
                        textValue = formatForUnit(preset.millis, selectedUnit)
                    },
                    label = {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }
        }
    }
}

// ============================================================================
// Formatting helpers
// ============================================================================

/** Pick the most natural unit for a given millisecond value. */
private fun bestUnitFor(millis: Long): TimeUnit = when {
    millis >= 60_000L && millis % 60_000L == 0L -> TimeUnit.MINUTES
    millis >= 1_000L -> TimeUnit.SECONDS
    else -> TimeUnit.MILLISECONDS
}

/** Convert millis to a string in the given unit, trimming trailing zeros. */
private fun formatForUnit(millis: Long, unit: TimeUnit): String {
    val value = millis.toDouble() / unit.toMillis
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toBigDecimal().stripTrailingZeros().toPlainString()
    }
}

/** Human-readable display string, e.g. "1.5s" or "500ms". */
private fun formatDisplay(millis: Long): String = when {
    millis >= 60_000L && millis % 60_000L == 0L -> "${millis / 60_000}m"
    millis >= 1_000L -> {
        val seconds = millis.toDouble() / 1_000
        if (seconds == seconds.toLong().toDouble()) "${seconds.toLong()}s"
        else "${seconds}s"
    }
    else -> "${millis}ms"
}
