package com.alfredorosas.vet2myfriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
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
import com.alfredorosas.vet2myfriend.feature.directory.RouteViewModel
import com.alfredorosas.vet2myfriend.ui.theme.Vet2MyFriendTheme

class MainActivity : ComponentActivity() {
    private val routeViewModel by viewModels<RouteViewModel> {
        viewModelFactory {
            initializer {
                val app = application as Vet2MyFriendApplication
                RouteViewModel(app.routingRepository, app.geocodingRepository, app.locationProvider)
            }
        }
    }
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
                        val routeState by routeViewModel.uiState.collectAsStateWithLifecycle()
                        val requestLocation = rememberLocationPermissionRequest(
                            onGranted = routeViewModel::useCurrentLocation,
                            onDenied = routeViewModel::locationPermissionDenied,
                        )
                        BackHandler(enabled = routeState.clinic != null) { routeViewModel.close() }
                        DirectoryScreen(
                            state = state,
                            onRetry = directoryViewModel::retry,
                            mapsConfigured = BuildConfig.MAPS_CONFIGURED,
                            routeState = routeState,
                            onRouteAction = routeViewModel::onAction,
                            onRequestLocation = requestLocation,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}
