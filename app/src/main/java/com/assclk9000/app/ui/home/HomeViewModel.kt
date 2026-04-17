package com.assclk9000.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assclk9000.app.data.model.ClickProfile
import com.assclk9000.app.data.repository.ProfileRepository
import com.assclk9000.app.data.repository.SettingsRepository
import com.assclk9000.app.service.ClickEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val clickEngine: ClickEngine
) : ViewModel() {

    val profiles: StateFlow<List<ClickProfile>> = profileRepository.getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val engineState: StateFlow<ClickEngine.EngineState> = clickEngine.state

    fun deleteProfile(profile: ClickProfile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
        }
    }

    fun startProfile(profile: ClickProfile) {
        viewModelScope.launch {
            val actions = profileRepository.getActionsForProfile(profile.id).first()
            if (actions.isEmpty()) return@launch

            val humanIntensity = settingsRepository.humanSimulationIntensity.first()
            clickEngine.start(profile, actions, humanIntensity)
        }
    }

    fun stopExecution() {
        clickEngine.stop()
    }
}
