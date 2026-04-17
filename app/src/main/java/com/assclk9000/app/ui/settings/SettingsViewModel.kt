package com.assclk9000.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assclk9000.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme: StateFlow<String> = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    val defaultInterval: StateFlow<Long> = settingsRepository.defaultInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000L)

    val hapticFeedback: StateFlow<Boolean> = settingsRepository.hapticFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val overlayOpacity: StateFlow<Float> = settingsRepository.overlayOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.8f)

    val overlaySize: StateFlow<Float> = settingsRepository.overlaySize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val discreteMode: StateFlow<Boolean> = settingsRepository.discreteMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val genericNotifications: StateFlow<Boolean> = settingsRepository.genericNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val humanSimulationIntensity: StateFlow<Float> = settingsRepository.humanSimulationIntensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0f)

    fun setTheme(value: String) {
        viewModelScope.launch { settingsRepository.setTheme(value) }
    }

    fun setDefaultInterval(value: Long) {
        viewModelScope.launch { settingsRepository.setDefaultInterval(value) }
    }

    fun setHapticFeedback(value: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticFeedback(value) }
    }

    fun setOverlayOpacity(value: Float) {
        viewModelScope.launch { settingsRepository.setOverlayOpacity(value) }
    }

    fun setOverlaySize(value: Float) {
        viewModelScope.launch { settingsRepository.setOverlaySize(value) }
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
}
