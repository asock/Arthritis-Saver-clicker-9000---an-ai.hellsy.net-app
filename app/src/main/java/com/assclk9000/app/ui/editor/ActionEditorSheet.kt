package com.assclk9000.app.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.assclk9000.app.data.model.ActionCondition
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ConditionType
import com.assclk9000.app.data.model.GestureType
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtRed

/**
 * Modal Bottom Sheet composable for editing a single [ClickAction].
 *
 * @param action The action to edit, or null to create a new action.
 * @param onSave Called with the resulting [ClickAction] when the user taps Save.
 * @param onDismiss Called when the sheet is dismissed or the user taps Cancel.
 * @param onPickCoordinates Called when the user wants to pick coordinates on screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActionEditorSheet(
    action: ClickAction?,
    onSave: (ClickAction) -> Unit,
    onDismiss: () -> Unit,
    onPickCoordinates: () -> Unit
) {
    // Local mutable state for editing — initialized from the action parameter
    var gestureType by remember { mutableStateOf(action?.gestureType ?: GestureType.TAP) }
    var x by remember { mutableFloatStateOf(action?.x ?: 540f) }
    var y by remember { mutableFloatStateOf(action?.y ?: 960f) }
    var x2 by remember { mutableFloatStateOf(action?.x2 ?: 540f) }
    var y2 by remember { mutableFloatStateOf(action?.y2 ?: 1200f) }
    var duration by remember { mutableLongStateOf(action?.duration ?: 50L) }
    var intervalAfter by remember { mutableLongStateOf(action?.intervalAfter ?: 1000L) }
    var jitterMs by remember { mutableLongStateOf(action?.jitterMs ?: 0L) }
    var jitterPx by remember { mutableFloatStateOf(action?.jitterPx ?: 0f) }
    var repeatCount by remember { mutableIntStateOf(action?.repeatCount ?: 1) }
    var description by remember { mutableStateOf(action?.description ?: "") }
    var conditionType by remember {
        mutableStateOf(action?.condition?.type ?: ConditionType.NONE)
    }
    var similarityThreshold by remember {
        mutableFloatStateOf(action?.condition?.similarityThreshold ?: 0.9f)
    }

    var showJitter by rememberSaveable { mutableStateOf(false) }
    var showConditions by rememberSaveable { mutableStateOf(false) }

    val isEditing = action != null

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val crtTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CrtPurple,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = CrtPurple,
        focusedLabelColor = CrtPurple,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Title ──────────────────────────────────────────────────
            Text(
                text = if (isEditing) "> EDIT ACTION_" else "> NEW ACTION_",
                style = MaterialTheme.typography.titleMedium,
                color = CrtPurple
            )

            // ── GestureType selector (FilterChip row) ──────────────────
            Text(
                text = "Gesture Type",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GestureType.entries.forEach { type ->
                    FilterChip(
                        selected = gestureType == type,
                        onClick = { gestureType = type },
                        label = {
                            Text(
                                text = gestureDisplayName(type),
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Coordinate inputs ──────────────────────────────────────
            if (gestureType != GestureType.WAIT) {
                Text(
                    text = "Coordinates",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Primary X / Y
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = x.toInt().toString(),
                        onValueChange = { text -> text.toFloatOrNull()?.let { x = it } },
                        label = { Text("X") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        colors = crtTextFieldColors
                    )
                    OutlinedTextField(
                        value = y.toInt().toString(),
                        onValueChange = { text -> text.toFloatOrNull()?.let { y = it } },
                        label = { Text("Y") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        colors = crtTextFieldColors
                    )
                }

                // "Pick on Screen" button
                OutlinedButton(
                    onClick = onPickCoordinates,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CrtPurple
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Pick on Screen",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Secondary X2 / Y2 for SWIPE
                if (gestureType == GestureType.SWIPE) {
                    Text(
                        text = "End Coordinates",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = x2.toInt().toString(),
                            onValueChange = { text -> text.toFloatOrNull()?.let { x2 = it } },
                            label = { Text("X2") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            colors = crtTextFieldColors
                        )
                        OutlinedTextField(
                            value = y2.toInt().toString(),
                            onValueChange = { text -> text.toFloatOrNull()?.let { y2 = it } },
                            label = { Text("Y2") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            colors = crtTextFieldColors
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Duration picker (OutlinedTextField with ms suffix) ─────
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { text -> text.toLongOrNull()?.let { duration = it } },
                label = { Text("Duration") },
                suffix = { Text("ms") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = crtTextFieldColors
            )

            // ── Interval After picker (OutlinedTextField with ms suffix)
            OutlinedTextField(
                value = intervalAfter.toString(),
                onValueChange = { text -> text.toLongOrNull()?.let { intervalAfter = it } },
                label = { Text("Interval After") },
                suffix = { Text("ms") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = crtTextFieldColors
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Repeat count (0 = infinite, show infinity symbol) ──────
            OutlinedTextField(
                value = repeatCount.toString(),
                onValueChange = { text -> text.toIntOrNull()?.let { repeatCount = it } },
                label = { Text("Repeat Count") },
                suffix = {
                    if (repeatCount == 0) {
                        Text(
                            text = "\u221E",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrtGreen
                        )
                    }
                },
                supportingText = {
                    if (repeatCount == 0) {
                        Text(
                            text = "0 = infinite repeats",
                            color = CrtGreen
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = crtTextFieldColors
            )

            // ── Description ────────────────────────────────────────────
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = crtTextFieldColors
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Expandable "Jitter" section ────────────────────────────
            TextButton(
                onClick = { showJitter = !showJitter },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showJitter) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CrtPurple
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Jitter (Humanization)",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrtPurple
                )
            }

            AnimatedVisibility(
                visible = showJitter,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Timing jitter slider (0–500ms)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Timing Jitter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${jitterMs}ms",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrtPurple
                            )
                        }
                        Slider(
                            value = jitterMs.toFloat(),
                            onValueChange = { jitterMs = it.toLong() },
                            valueRange = 0f..500f,
                            steps = 49,
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

                    // Position jitter slider (0–10px)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Position Jitter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${jitterPx.toInt()}px",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrtPurple
                            )
                        }
                        Slider(
                            value = jitterPx,
                            onValueChange = { jitterPx = it },
                            valueRange = 0f..10f,
                            steps = 9,
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
            }

            // ── Expandable "Condition" section ─────────────────────────
            TextButton(
                onClick = { showConditions = !showConditions },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showConditions) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CrtPurple
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Condition Settings",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrtPurple
                )
            }

            AnimatedVisibility(
                visible = showConditions,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ConditionType dropdown
                    Text(
                        text = "Condition Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    var conditionDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = conditionDropdownExpanded,
                        onExpandedChange = { conditionDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = conditionDisplayName(conditionType),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = conditionDropdownExpanded
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            colors = crtTextFieldColors
                        )
                        ExposedDropdownMenu(
                            expanded = conditionDropdownExpanded,
                            onDismissRequest = { conditionDropdownExpanded = false }
                        ) {
                            ConditionType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = conditionDisplayName(type),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        conditionType = type
                                        conditionDropdownExpanded = false
                                    },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                )
                            }
                        }
                    }

                    // Similarity threshold slider (0.5–1.0)
                    if (conditionType != ConditionType.NONE) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Similarity Threshold",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(similarityThreshold * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CrtGreen
                                )
                            }
                            Slider(
                                value = similarityThreshold,
                                onValueChange = { similarityThreshold = it },
                                valueRange = 0.5f..1.0f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = CrtGreen,
                                    activeTrackColor = CrtGreen,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Save / Cancel buttons ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CrtRed
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = {
                        val condition = if (conditionType != ConditionType.NONE) {
                            ActionCondition(
                                type = conditionType,
                                similarityThreshold = similarityThreshold,
                                imageData = action?.condition?.imageData,
                                regionX = action?.condition?.regionX ?: 0,
                                regionY = action?.condition?.regionY ?: 0,
                                regionWidth = action?.condition?.regionWidth ?: 0,
                                regionHeight = action?.condition?.regionHeight ?: 0
                            )
                        } else {
                            null
                        }

                        val result = ClickAction(
                            id = action?.id ?: 0L,
                            profileId = action?.profileId ?: 0L,
                            orderIndex = action?.orderIndex ?: 0,
                            gestureType = gestureType,
                            x = x,
                            y = y,
                            x2 = if (gestureType == GestureType.SWIPE) x2 else null,
                            y2 = if (gestureType == GestureType.SWIPE) y2 else null,
                            duration = duration,
                            intervalAfter = intervalAfter,
                            jitterMs = jitterMs,
                            jitterPx = jitterPx,
                            repeatCount = repeatCount,
                            condition = condition,
                            description = description
                        )
                        onSave(result)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrtGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Bottom spacing for navigation bar
            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun gestureDisplayName(type: GestureType): String = when (type) {
    GestureType.TAP -> "Tap"
    GestureType.LONG_PRESS -> "Long Press"
    GestureType.SWIPE -> "Swipe"
    GestureType.PINCH -> "Pinch"
    GestureType.CUSTOM_PATH -> "Custom Path"
    GestureType.WAIT -> "Wait"
}

private fun conditionDisplayName(type: ConditionType): String = when (type) {
    ConditionType.NONE -> "None"
    ConditionType.WAIT_FOR_IMAGE -> "Wait for Image"
    ConditionType.CLICK_ON_IMAGE -> "Click on Image"
    ConditionType.STOP_IF_IMAGE_GONE -> "Stop if Gone"
}
