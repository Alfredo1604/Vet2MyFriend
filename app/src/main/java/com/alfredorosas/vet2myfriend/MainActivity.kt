package com.alfredorosas.vet2myfriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alfredorosas.vet2myfriend.feature.directory.DirectoryScreen
import com.alfredorosas.vet2myfriend.feature.directory.DirectoryViewModel
import com.alfredorosas.vet2myfriend.ui.theme.Vet2MyFriendTheme

class MainActivity : ComponentActivity() {
    private val directoryViewModel by viewModels<DirectoryViewModel> {
        viewModelFactory {
            initializer {
                val app = application as Vet2MyFriendApplication
                DirectoryViewModel(app.clinicRepository)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = getSharedPreferences("onboarding", MODE_PRIVATE)
        val welcomeCompleted = preferences.getBoolean("welcome_completed", false)
        setContent {
            var showWelcome by rememberSaveable { mutableStateOf(!welcomeCompleted) }
            Vet2MyFriendTheme {
                if (showWelcome) {
                    WelcomeScreen {
                        preferences.edit().putBoolean("welcome_completed", true).apply()
                        showWelcome = false
                    }
                } else {
                    AppShell { modifier ->
                        val state by directoryViewModel.uiState.collectAsStateWithLifecycle()
                        DirectoryScreen(state, directoryViewModel::retry, modifier)
                    }
                }
            }
        }
    }
}
