package com.alfredorosas.vet2myfriend

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private enum class HomeTab(@field:StringRes val label: Int, @field:DrawableRes val icon: Int) {
    HOME(R.string.tab_home, android.R.drawable.ic_menu_mylocation),
    ROUTES(R.string.tab_routes, android.R.drawable.ic_menu_directions),
    DIARY(R.string.tab_diary, android.R.drawable.ic_menu_edit),
    SETTINGS(R.string.tab_settings, android.R.drawable.ic_menu_preferences),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppShell(home: @Composable (Modifier) -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.HOME) }
    BackHandler(enabled = selectedTab != HomeTab.HOME) { selectedTab = HomeTab.HOME }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { padding ->
        val content = Modifier.fillMaxSize().padding(padding)
        if (selectedTab == HomeTab.HOME) {
            home(content)
        } else {
            Box(content.padding(24.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.section_unavailable, stringResource(selectedTab.label)))
            }
        }
    }
}

@Composable
internal fun WelcomeScreen(onContinue: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.welcome_description))
            Button(onClick = onContinue) { Text(stringResource(R.string.welcome_continue)) }
        }
    }
}
