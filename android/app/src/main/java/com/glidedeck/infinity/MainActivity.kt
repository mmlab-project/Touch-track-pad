package com.glidedeck.infinity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.glidedeck.infinity.ui.KeyboardScreen
import com.glidedeck.infinity.ui.QrScannerScreen
import com.glidedeck.infinity.ui.TrackpadScreen
import com.glidedeck.infinity.ui.MacroScreen
import com.glidedeck.infinity.ui.theme.GlideDeckTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Haptic Initialization
        // viewModel.hapticManager...
        
        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = "Dark")
            val isDarkTheme = themeMode == "Dark"
            
            GlideDeckTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val isConnected by viewModel.isConnected.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = if (isConnected) "trackpad" else "scanner"
                ) {
                    composable("scanner") {
                        QrScannerScreen(viewModel = viewModel)
                    }
                    composable("trackpad") {
                        TrackpadScreen(
                            viewModel = viewModel,
                            onNavigateToKeyboard = { navController.navigate("keyboard") },
                            onNavigateToMacros = { navController.navigate("macros") }
                        )
                    }
                    composable("macros") {
                        MacroScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("keyboard") {
                        KeyboardScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                
                // Connection State Observer to navigate automatically
                // Note: SideEffect inside Composable is preferred
                // If becomes connected while in scanner, move to trackpad
                if (isConnected && navController.currentDestination?.route == "scanner") {
                    navController.navigate("trackpad") {
                        popUpTo("scanner") { inclusive = true }
                    }
                }
                
                // If disconnected while in trackpad/keyboard, move to scanner
                if (!isConnected && navController.currentDestination?.route != "scanner") {
                    navController.navigate("scanner") {
                        popUpTo("trackpad") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

