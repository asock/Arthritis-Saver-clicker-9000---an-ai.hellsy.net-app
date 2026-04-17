package com.assclk9000.app.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import com.assclk9000.app.service.ClickerAccessibilityService
import com.assclk9000.app.util.PermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    init {
        checkPermissions()
    }

    /**
     * Re-checks accessibility service and overlay permissions.
     * Should be called on Activity resume to pick up changes made in system settings.
     */
    fun checkPermissions() {
        val accessibilityOk = PermissionHelper.isAccessibilityServiceEnabled(
            appContext,
            ClickerAccessibilityService::class.java
        )
        val overlayOk = PermissionHelper.isOverlayPermissionGranted(appContext)
        _permissionsGranted.value = accessibilityOk && overlayOk
    }
}
