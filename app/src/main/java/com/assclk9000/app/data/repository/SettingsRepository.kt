package com.assclk9000.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_DEFAULT_INTERVAL = longPreferencesKey("default_interval")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_OVERLAY_OPACITY = floatPreferencesKey("overlay_opacity")
        val KEY_OVERLAY_SIZE = floatPreferencesKey("overlay_size")
        val KEY_DISCRETE_MODE = booleanPreferencesKey("discrete_mode")
        val KEY_GENERIC_NOTIFICATIONS = booleanPreferencesKey("generic_notifications")
        val KEY_HUMAN_SIMULATION_INTENSITY = floatPreferencesKey("human_simulation_intensity")
    }

    // Theme (light / dark / system)
    val theme: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "system"
    }

    suspend fun setTheme(value: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = value
        }
    }

    // Default interval (ms)
    val defaultInterval: Flow<Long> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_INTERVAL] ?: 1000L
    }

    suspend fun setDefaultInterval(value: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_INTERVAL] = value
        }
    }

    // Haptic feedback
    val hapticFeedback: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_HAPTIC_FEEDBACK] ?: true
    }

    suspend fun setHapticFeedback(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_FEEDBACK] = value
        }
    }

    // Overlay opacity
    val overlayOpacity: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_OVERLAY_OPACITY] ?: 0.8f
    }

    suspend fun setOverlayOpacity(value: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_OPACITY] = value
        }
    }

    // Overlay size
    val overlaySize: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_OVERLAY_SIZE] ?: 1.0f
    }

    suspend fun setOverlaySize(value: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_OVERLAY_SIZE] = value
        }
    }

    // Discrete mode (stealth - hide overlay, notification only)
    val discreteMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DISCRETE_MODE] ?: false
    }

    suspend fun setDiscreteMode(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DISCRETE_MODE] = value
        }
    }

    // Generic notifications (stealth - system-style notifications)
    val genericNotifications: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_GENERIC_NOTIFICATIONS] ?: false
    }

    suspend fun setGenericNotifications(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_GENERIC_NOTIFICATIONS] = value
        }
    }

    // Human simulation intensity (0.0 - 1.0, controls jitter/drift amount)
    val humanSimulationIntensity: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_HUMAN_SIMULATION_INTENSITY] ?: 0.0f
    }

    suspend fun setHumanSimulationIntensity(value: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_HUMAN_SIMULATION_INTENSITY] = value.coerceIn(0.0f, 1.0f)
        }
    }
}
