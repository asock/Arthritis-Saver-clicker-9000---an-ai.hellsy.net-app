package com.assclk9000.app.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.assclk9000.app.ui.components.PermissionGate
import com.assclk9000.app.ui.editor.ProfileEditorScreen
import com.assclk9000.app.ui.home.HomeScreen
import com.assclk9000.app.ui.settings.SettingsScreen
import com.assclk9000.app.ui.theme.ArthritisSaverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ArthritisSaverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check permissions when activity resumes (user may have granted them in settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Observe but don't gate here -- PermissionGate handles the gating UI
    @Suppress("UNUSED_VARIABLE")
    val permissionsGranted by mainViewModel.permissionsGranted.collectAsState()

    PermissionGate {
        AppNavigation()
    }
}

@Composable
private fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home screen
        composable("home") {
            HomeScreen(
                onNavigateToEditor = { profileId ->
                    navController.navigate("editor/$profileId")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // Profile editor
        composable(
            route = "editor/{profileId}",
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            ProfileEditorScreen(
                profileId = profileId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
