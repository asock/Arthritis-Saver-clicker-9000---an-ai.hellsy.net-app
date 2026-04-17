package com.assclk9000.app.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Arthritis Saver Clicker 9000 - CRT Purple Hacker Palette
// Inspired by 90s terminal aesthetics, phosphor glow, and hellsy.net vibes
// =============================================================================

// --- Primary Neon Purples ---
val CrtPurple = Color(0xFFBB33FF)           // Bright neon purple - primary actions
val CrtPurpleDark = Color(0xFF7B1FA2)       // Deeper purple - pressed/container states
val CrtPurpleGlow = Color(0x40BB33FF)       // Purple with alpha for glow/shadow effects

// --- Accent Colors ---
val CrtMagenta = Color(0xFFFF33CC)          // Hot magenta - secondary accent
val CrtGreen = Color(0xFF33FF33)            // Phosphor green - active/running states
val CrtGreenDim = Color(0xFF1A8A1A)         // Dimmed green - enabled but idle
val CrtAmber = Color(0xFFFFBB33)            // Amber - warnings
val CrtRed = Color(0xFFFF3333)              // CRT red - error/stop/delete

// --- Dark Theme Surfaces ---
val CrtBackground = Color(0xFF0D0D1A)       // Near-black with purple tint
val CrtSurface = Color(0xFF1A1A2E)          // Slightly lighter surface
val CrtSurfaceVariant = Color(0xFF252540)   // Card backgrounds
val CrtSurfaceBright = Color(0xFF2E2E4A)    // Elevated surfaces / dialogs

// --- Dark Theme Text ---
val CrtOnBackground = Color(0xFFE0E0FF)     // Light lavender - primary text
val CrtOnSurface = Color(0xFFCCCCFF)        // Slightly dimmer - secondary text
val CrtOnSurfaceDim = Color(0xFF8888AA)     // Dimmed text - hints, placeholders
val CrtOnPrimary = Color(0xFF0D0D1A)        // Dark text on primary color buttons
val CrtOnError = Color(0xFFFFFFFF)          // White text on error backgrounds

// --- Outline / Divider ---
val CrtOutline = Color(0xFF3A3A5C)          // Subtle purple-grey border
val CrtOutlineVariant = Color(0xFF2A2A44)   // Even subtler divider

// --- Light Theme Fallback (still purple-tinted) ---
val CrtLightBackground = Color(0xFFF0EEFF)
val CrtLightSurface = Color(0xFFFFFFFF)
val CrtLightSurfaceVariant = Color(0xFFE8E0F0)
val CrtLightPrimary = Color(0xFF7B1FA2)
val CrtLightOnBackground = Color(0xFF1A1A2E)
val CrtLightOnSurface = Color(0xFF252540)
val CrtLightOnSurfaceDim = Color(0xFF6A6A8A)
val CrtLightOutline = Color(0xFFB0A8C8)
