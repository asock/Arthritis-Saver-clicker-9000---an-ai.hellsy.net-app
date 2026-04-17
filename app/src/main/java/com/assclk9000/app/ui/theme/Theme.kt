package com.assclk9000.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// =============================================================================
// Dark color scheme -- the default CRT purple hacker aesthetic
// =============================================================================
private val CrtDarkColorScheme = darkColorScheme(
    primary = CrtPurple,
    onPrimary = CrtOnPrimary,
    primaryContainer = CrtPurpleDark,
    onPrimaryContainer = CrtOnBackground,

    secondary = CrtMagenta,
    onSecondary = CrtOnPrimary,
    secondaryContainer = CrtPurpleDark,
    onSecondaryContainer = CrtOnBackground,

    tertiary = CrtGreen,
    onTertiary = CrtOnPrimary,
    tertiaryContainer = CrtGreenDim,
    onTertiaryContainer = CrtOnBackground,

    error = CrtRed,
    onError = CrtOnError,
    errorContainer = Color(0xFF400000),
    onErrorContainer = CrtRed,

    background = CrtBackground,
    onBackground = CrtOnBackground,

    surface = CrtSurface,
    onSurface = CrtOnSurface,
    surfaceVariant = CrtSurfaceVariant,
    onSurfaceVariant = CrtOnSurfaceDim,

    outline = CrtOutline,
    outlineVariant = CrtOutlineVariant,

    inverseSurface = CrtOnBackground,
    inverseOnSurface = CrtBackground,
    inversePrimary = CrtPurpleDark
)

// =============================================================================
// Light color scheme -- purple-tinted fallback for users who prefer light mode
// =============================================================================
private val CrtLightColorScheme = lightColorScheme(
    primary = CrtLightPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8D0FF),
    onPrimaryContainer = Color(0xFF2E004E),

    secondary = Color(0xFFCC2299),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD6F0),
    onSecondaryContainer = Color(0xFF3E0028),

    tertiary = Color(0xFF1A8A1A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB8F5B8),
    onTertiaryContainer = Color(0xFF002E00),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = CrtLightBackground,
    onBackground = CrtLightOnBackground,

    surface = CrtLightSurface,
    onSurface = CrtLightOnSurface,
    surfaceVariant = CrtLightSurfaceVariant,
    onSurfaceVariant = CrtLightOnSurfaceDim,

    outline = CrtLightOutline,
    outlineVariant = Color(0xFFD0C8E0),

    inverseSurface = CrtLightOnBackground,
    inverseOnSurface = CrtLightBackground,
    inversePrimary = CrtPurple
)

// =============================================================================
// Convenience: a Color import for inline hex colors above
// =============================================================================
private typealias Color = androidx.compose.ui.graphics.Color

// =============================================================================
// Main theme composable
// =============================================================================

/**
 * Arthritis Saver Clicker 9000 Material 3 theme.
 *
 * @param darkTheme Whether to apply the dark CRT hacker palette. Defaults to
 *   the system setting.
 * @param dynamicColor If true **and** the device runs Android 12+, the theme
 *   will use Material You dynamic color. Defaults to false so the curated CRT
 *   palette is shown by default.
 */
@Composable
fun ArthritisSaverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> CrtDarkColorScheme
        else -> CrtLightColorScheme
    }

    // Tint the system status bar to match our background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArthritisSaverTypography,
        content = content
    )
}
