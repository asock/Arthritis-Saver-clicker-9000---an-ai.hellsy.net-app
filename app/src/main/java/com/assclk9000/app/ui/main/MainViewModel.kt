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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val accessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(
            context,
            ClickerAccessibilityService::class.java
        )
        val overlayGranted = PermissionHelper.isOverlayPermissionGranted(context)
        _permissionsGranted.value = accessibilityEnabled && overlayGranted
    }
}
