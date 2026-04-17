package com.assclk9000.app.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ClickProfile
import com.assclk9000.app.data.model.ScheduleConfig
import com.assclk9000.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<ClickProfile?>(null)
    val profile: StateFlow<ClickProfile?> = _profile.asStateFlow()

    private val _actions = MutableStateFlow<List<ClickAction>>(emptyList())
    val actions: StateFlow<List<ClickAction>> = _actions.asStateFlow()

    private var isNewProfile = false

    /**
     * Load an existing profile or initialize a new blank one when [profileId] is -1.
     */
    fun loadProfile(profileId: Long) {
        viewModelScope.launch {
            if (profileId == -1L) {
                isNewProfile = true
                val now = System.currentTimeMillis()
                _profile.value = ClickProfile(
                    name = "",
                    description = "",
                    createdAt = now,
                    updatedAt = now
                )
                _actions.value = emptyList()
            } else {
                isNewProfile = false
                val existingProfile = profileRepository.getProfileById(profileId).first()
                _profile.value = existingProfile
                if (existingProfile != null) {
                    val existingActions = profileRepository.getActionsForProfile(profileId).first()
                    _actions.value = existingActions
                }
            }
        }
    }

    fun updateProfileName(name: String) {
        _profile.value = _profile.value?.copy(name = name, updatedAt = System.currentTimeMillis())
    }

    fun updateProfileDescription(description: String) {
        _profile.value = _profile.value?.copy(
            description = description,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateGlobalRepeatCount(count: Int) {
        _profile.value = _profile.value?.copy(
            globalRepeatCount = count,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateGlobalIntervalMs(intervalMs: Long) {
        _profile.value = _profile.value?.copy(
            globalIntervalMs = intervalMs,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateScheduleConfig(config: ScheduleConfig) {
        _profile.value = _profile.value?.copy(
            scheduleConfig = config,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun addAction(action: ClickAction) {
        val current = _actions.value.toMutableList()
        val reIndexed = action.copy(orderIndex = current.size)
        current.add(reIndexed)
        _actions.value = current
    }

    fun updateAction(action: ClickAction) {
        _actions.value = _actions.value.map {
            if (it.orderIndex == action.orderIndex) action else it
        }
    }

    fun deleteAction(action: ClickAction) {
        val remaining = _actions.value
            .filter { it.orderIndex != action.orderIndex }
            .mapIndexed { index, a -> a.copy(orderIndex = index) }
        _actions.value = remaining
    }

    fun reorderActions(fromIndex: Int, toIndex: Int) {
        val list = _actions.value.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _actions.value = list.mapIndexed { index, a -> a.copy(orderIndex = index) }
    }

    /**
     * Persists the profile and all its actions to the database.
     * For new profiles, inserts them; for existing ones, updates.
     */
    fun saveProfile(onSaved: () -> Unit = {}) {
        val currentProfile = _profile.value ?: return
        viewModelScope.launch {
            if (isNewProfile) {
                val profileId = profileRepository.insertProfile(
                    currentProfile.copy(updatedAt = System.currentTimeMillis())
                )
                val actionsWithProfileId = _actions.value.map { it.copy(profileId = profileId) }
                if (actionsWithProfileId.isNotEmpty()) {
                    profileRepository.insertActions(actionsWithProfileId)
                }
            } else {
                profileRepository.updateProfile(
                    currentProfile.copy(updatedAt = System.currentTimeMillis())
                )
                // Delete existing actions and re-insert to handle reorders, additions, deletions
                profileRepository.deleteActionsForProfile(currentProfile.id)
                val actionsWithProfileId = _actions.value.map {
                    it.copy(id = 0L, profileId = currentProfile.id)
                }
                if (actionsWithProfileId.isNotEmpty()) {
                    profileRepository.insertActions(actionsWithProfileId)
                }
            }
            onSaved()
        }
    }
}
