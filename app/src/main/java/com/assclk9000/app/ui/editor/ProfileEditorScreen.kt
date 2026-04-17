package com.assclk9000.app.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.GestureType
import com.assclk9000.app.ui.components.ActionCard
import com.assclk9000.app.ui.components.IntervalPicker
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    profileId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ProfileEditorViewModel = hiltViewModel()
) {
    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }

    val profile by viewModel.profile.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val scope = rememberCoroutineScope()

    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }
    var editingAction by remember { mutableStateOf<ClickAction?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Text field colors in CRT purple style
    val crtTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CrtPurple,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = CrtPurple,
        focusedLabelColor = CrtPurple,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (profileId == -1L) "> NEW PROFILE_" else "> EDIT PROFILE_",
                        style = MaterialTheme.typography.titleMedium,
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
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveProfile { onNavigateBack() }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save profile",
                            tint = CrtGreen
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
        if (profile == null) return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile name
            item {
                OutlinedTextField(
                    value = profile?.name ?: "",
                    onValueChange = { viewModel.updateProfileName(it) },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = crtTextFieldColors,
                    textStyle = MaterialTheme.typography.titleMedium
                )
            }

            // Profile description
            item {
                OutlinedTextField(
                    value = profile?.description ?: "",
                    onValueChange = { viewModel.updateProfileDescription(it) },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = crtTextFieldColors,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            // Advanced section header
            item {
                TextButton(
                    onClick = { showAdvanced = !showAdvanced },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (showAdvanced) Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = CrtPurple
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Advanced Settings",
                        style = MaterialTheme.typography.labelLarge,
                        color = CrtPurple
                    )
                }
            }

            // Advanced section content
            item {
                AnimatedVisibility(
                    visible = showAdvanced,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Global repeat count
                        OutlinedTextField(
                            value = (profile?.globalRepeatCount ?: 1).toString(),
                            onValueChange = { text ->
                                text.toIntOrNull()?.let { viewModel.updateGlobalRepeatCount(it) }
                            },
                            label = { Text("Global Repeat Count (0 = infinite)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = crtTextFieldColors,
                            textStyle = MaterialTheme.typography.titleMedium
                        )

                        // Global interval
                        IntervalPicker(
                            valueMillis = profile?.globalIntervalMs ?: 0L,
                            onValueChange = { viewModel.updateGlobalIntervalMs(it) },
                            label = "Global Interval (between cycles)"
                        )

                        // Schedule config - delay before start
                        IntervalPicker(
                            valueMillis = profile?.scheduleConfig?.delayBeforeStart ?: 0L,
                            onValueChange = { delay ->
                                profile?.scheduleConfig?.let { config ->
                                    viewModel.updateScheduleConfig(config.copy(delayBeforeStart = delay))
                                }
                            },
                            label = "Delay Before Start"
                        )

                        // Schedule config - stop after time
                        IntervalPicker(
                            valueMillis = profile?.scheduleConfig?.stopAfterMs ?: 0L,
                            onValueChange = { stopMs ->
                                profile?.scheduleConfig?.let { config ->
                                    viewModel.updateScheduleConfig(config.copy(stopAfterMs = stopMs))
                                }
                            },
                            label = "Stop After Time (0 = never)"
                        )

                        // Schedule config - stop after repeats
                        OutlinedTextField(
                            value = (profile?.scheduleConfig?.stopAfterRepeats ?: 0).toString(),
                            onValueChange = { text ->
                                text.toIntOrNull()?.let { repeats ->
                                    profile?.scheduleConfig?.let { config ->
                                        viewModel.updateScheduleConfig(
                                            config.copy(stopAfterRepeats = repeats)
                                        )
                                    }
                                }
                            },
                            label = { Text("Stop After Repeats (0 = never)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = crtTextFieldColors,
                            textStyle = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Section header: Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> ACTIONS (${actions.size})_",
                        style = MaterialTheme.typography.titleSmall,
                        color = CrtPurple
                    )
                }
            }

            // Actions list
            itemsIndexed(actions, key = { _, action -> action.orderIndex }) { index, action ->
                ActionCard(
                    action = action,
                    onEdit = {
                        editingAction = action
                        showActionSheet = true
                    },
                    onDelete = { viewModel.deleteAction(action) },
                    showDragHandle = true
                )
            }

            // Add action button
            item {
                Button(
                    onClick = {
                        editingAction = null
                        showActionSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrtPurple,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Add Action",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    // Action editor bottom sheet
    if (showActionSheet) {
        ActionEditorSheet(
            action = editingAction,
            onSave = { action ->
                if (editingAction != null) {
                    viewModel.updateAction(action)
                } else {
                    viewModel.addAction(action)
                }
                scope.launch {
                    sheetState.hide()
                    showActionSheet = false
                    editingAction = null
                }
            },
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    showActionSheet = false
                    editingAction = null
                }
            },
            onPickCoordinates = {
                // TODO: Launch TargetPickerOverlay via overlay service
            }
        )
    }
}
