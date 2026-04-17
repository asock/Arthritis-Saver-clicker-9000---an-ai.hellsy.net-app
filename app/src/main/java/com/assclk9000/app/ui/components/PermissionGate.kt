package com.assclk9000.app.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.assclk9000.app.service.ClickerAccessibilityService
import com.assclk9000.app.ui.theme.CrtGreen
import com.assclk9000.app.ui.theme.CrtPurple
import com.assclk9000.app.ui.theme.CrtRed
import com.assclk9000.app.ui.theme.CrtSurfaceVariant
import com.assclk9000.app.ui.theme.CrtOutline
import com.assclk9000.app.util.PermissionHelper

// ============================================================================
// PermissionGate -- blocks content until all permissions are granted
// ============================================================================

/**
 * A gate composable that checks accessibility, overlay, and notification
 * permissions. Displays a permission checklist with grant buttons until
 * all required permissions are satisfied, then renders [content].
 *
 * @param modifier Modifier applied to the root layout.
 * @param content The child content to render once all permissions are granted.
 */
@Composable
fun PermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- Permission state, re-checked on every resume ---
    var accessibilityGranted by remember { mutableStateOf(false) }
    var overlayGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }

    // Re-check permissions when the user returns from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityGranted = PermissionHelper.isAccessibilityServiceEnabled(
                    context, ClickerAccessibilityService::class.java
                )
                overlayGranted = PermissionHelper.isOverlayPermissionGranted(context)
                notificationGranted = PermissionHelper.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allGranted = accessibilityGranted && overlayGranted && notificationGranted

    if (allGranted) {
        content()
    } else {
        PermissionChecklist(
            accessibilityGranted = accessibilityGranted,
            overlayGranted = overlayGranted,
            notificationGranted = notificationGranted,
            modifier = modifier
        )
    }
}

// ============================================================================
// PermissionChecklist -- the actual UI with status + grant buttons
// ============================================================================

@Composable
private fun PermissionChecklist(
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    notificationGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Notification permission launcher (API 33+)
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* state refreshes on resume via lifecycle observer */ }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Header ---
        Text(
            text = "PERMISSIONS REQUIRED",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Grant all permissions to enable the auto-clicker.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // --- Accessibility ---
        PermissionRow(
            icon = Icons.Default.Accessibility,
            title = "Accessibility Service",
            description = "Required to dispatch taps and gestures on your behalf.",
            isGranted = accessibilityGranted,
            onGrant = {
                PermissionHelper.openAccessibilitySettings(context)
            }
        )

        Spacer(Modifier.height(16.dp))

        // --- Overlay ---
        PermissionRow(
            icon = Icons.Default.Layers,
            title = "Overlay Permission",
            description = "Required to show the floating control panel and target picker.",
            isGranted = overlayGranted,
            onGrant = {
                val activity = context as? Activity
                if (activity != null) {
                    PermissionHelper.requestOverlayPermission(activity)
                } else {
                    // Fallback: open overlay settings without specific package
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // --- Notifications ---
        PermissionRow(
            icon = Icons.Default.Notifications,
            title = "Notification Permission",
            description = "Required for the foreground service notification while running.",
            isGranted = notificationGranted,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                // On older APIs this is automatically granted
            }
        )

        Spacer(Modifier.height(32.dp))

        // --- Status summary ---
        val granted = listOf(accessibilityGranted, overlayGranted, notificationGranted)
            .count { it }
        Text(
            text = "$granted / 3 permissions granted",
            style = MaterialTheme.typography.labelLarge,
            color = if (granted == 3) CrtGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// PermissionRow -- single permission status with grant button
// ============================================================================

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CrtSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (isGranted) CrtGreen.copy(alpha = 0.4f) else CrtOutline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon (check / X)
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = if (isGranted) "Granted" else "Not granted",
            tint = if (isGranted) CrtGreen else CrtRed,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.width(12.dp))

        // Permission icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) CrtGreen else CrtPurple,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(12.dp))

        // Text column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        // Grant button (hidden when already granted)
        if (!isGranted) {
            Button(
                onClick = onGrant,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrtPurple,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Grant",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
