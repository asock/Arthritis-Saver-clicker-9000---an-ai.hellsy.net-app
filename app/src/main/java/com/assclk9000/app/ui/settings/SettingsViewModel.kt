package com.assclk9000.app.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assclk9000.app.data.repository.ProfileRepository
import com.assclk9000.app.data.repository.SettingsRepository
import com.assclk9000.app.util.ProfileExporter
import com.google.gson.Gson
import com.google.gson.JsonArray
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsVM"
    }

    // --- Appearance ---

    val theme: StateFlow<String> = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), "system")

    val overlayOpacity: StateFlow<Float> = settingsRepository.overlayOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0.8f)

    val overlaySize: StateFlow<Float> = settingsRepository.overlaySize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 1.0f)

    // --- Behavior ---

    val defaultInterval: StateFlow<Long> = settingsRepository.defaultInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 1000L)

    val hapticFeedback: StateFlow<Boolean> = settingsRepository.hapticFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), true)

    // --- Stealth ---

    val discreteMode: StateFlow<Boolean> = settingsRepository.discreteMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    val genericNotifications: StateFlow<Boolean> = settingsRepository.genericNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    val humanSimulationIntensity: StateFlow<Float> = settingsRepository.humanSimulationIntensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0.0f)

    // --- Setters ---

    fun setTheme(value: String) {
        viewModelScope.launch { settingsRepository.setTheme(value) }
    }

    fun setOverlayOpacity(value: Float) {
        viewModelScope.launch { settingsRepository.setOverlayOpacity(value) }
    }

    fun setOverlaySize(value: Float) {
        viewModelScope.launch { settingsRepository.setOverlaySize(value) }
    }

    fun setDefaultInterval(value: Long) {
        viewModelScope.launch { settingsRepository.setDefaultInterval(value) }
    }

    fun setHapticFeedback(value: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticFeedback(value) }
    }

    fun setDiscreteMode(value: Boolean) {
        viewModelScope.launch { settingsRepository.setDiscreteMode(value) }
    }

    fun setGenericNotifications(value: Boolean) {
        viewModelScope.launch { settingsRepository.setGenericNotifications(value) }
    }

    fun setHumanSimulationIntensity(value: Float) {
        viewModelScope.launch { settingsRepository.setHumanSimulationIntensity(value) }
    }

    // --- Data: Export / Import ---

    /**
     * Exports all profiles and their actions to a single JSON file at the given URI.
     * Each profile is exported as a separate JSON object in a JSON array.
     */
    fun exportAllProfiles(uri: Uri) {
        viewModelScope.launch {
            try {
                val profiles = profileRepository.getAllProfiles().first()
                val allExports = profiles.map { profile ->
                    val actions = profileRepository.getActionsForProfile(profile.id).first()
                    ProfileExporter.exportProfile(profile, actions)
                }
                val json = "[\n${allExports.joinToString(",\n")}\n]"
                appContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                }
                Log.d(TAG, "Exported ${profiles.size} profiles")
            } catch (e: Exception) {
                Log.e(TAG, "Export failed", e)
            }
        }
    }

    /**
     * Imports profiles from a JSON file at the given URI.
     * Supports both single profile JSON and JSON array of profiles.
     */
    fun importProfiles(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: return@launch

                val trimmed = json.trim()
                if (trimmed.startsWith("[")) {
                    // Array of profiles
                    val gson = Gson()
                    val array = gson.fromJson(trimmed, JsonArray::class.java)
                    var imported = 0
                    for (element in array) {
                        val elementJson = element.toString()
                        val (profile, actions) = ProfileExporter.importProfile(elementJson)
                        val newId = profileRepository.insertProfile(
                            profile.copy(
                                id = 0L,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        val newActions = actions.map { it.copy(id = 0L, profileId = newId) }
                        if (newActions.isNotEmpty()) {
                            profileRepository.insertActions(newActions)
                        }
                        imported++
                    }
                    Log.d(TAG, "Imported $imported profiles")
                } else {
                    // Single profile
                    val (profile, actions) = ProfileExporter.importProfile(trimmed)
                    val newId = profileRepository.insertProfile(
                        profile.copy(
                            id = 0L,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    val newActions = actions.map { it.copy(id = 0L, profileId = newId) }
                    if (newActions.isNotEmpty()) {
                        profileRepository.insertActions(newActions)
                    }
                    Log.d(TAG, "Imported 1 profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
            }
        }
    }
}
