package com.assclk9000.app.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.assclk9000.app.ui.editor.ProfileEditorScreen
import com.assclk9000.app.ui.home.HomeScreen
import com.assclk9000.app.ui.settings.SettingsScreen
import com.assclk9000.app.ui.theme.ArthritisSaverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArthritisSaverTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
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
                    composable(
                        route = "editor/{profileId}",
                        arguments = listOf(
                            navArgument("profileId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) {
                        ProfileEditorScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
