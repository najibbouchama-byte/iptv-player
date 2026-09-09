package com.iptvplayer.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.ui.favorites.FavoritesScreen
import com.iptvplayer.app.ui.home.HomeScreen
import com.iptvplayer.app.ui.livetv.LiveTvScreen
import com.iptvplayer.app.ui.player.PlayerScreen
import com.iptvplayer.app.ui.search.SearchScreen
import com.iptvplayer.app.ui.settings.SettingsScreen

private data class Tab(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("home", R.string.nav_home, Icons.Filled.Home),
    Tab("live_tv", R.string.nav_live_tv, Icons.Filled.Tv),
    Tab("favorites", R.string.nav_favorites, Icons.Filled.Favorite),
    Tab("search", R.string.nav_search, Icons.Filled.Search),
    Tab("settings", R.string.nav_settings, Icons.Filled.Settings)
)

@Composable
fun MainNavHost(onLoggedOut: () -> Unit) {
    // Chaîne actuellement sélectionnée : si non nulle, on affiche le lecteur plein écran
    // par-dessus la navigation par onglets (plus simple et plus fiable que de faire transiter
    // un objet Channel complet à travers le système de navigation).
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }

    if (selectedChannel != null) {
        PlayerScreen(
            channel = selectedChannel!!,
            onBack = { selectedChannel = null }
        )
        return
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(onChannelClick = { selectedChannel = it })
            }
            composable("live_tv") {
                LiveTvScreen(onChannelClick = { selectedChannel = it })
            }
            composable("favorites") {
                FavoritesScreen(onChannelClick = { selectedChannel = it })
            }
            composable("search") {
                SearchScreen(onChannelClick = { selectedChannel = it })
            }
            composable("settings") {
                SettingsScreen(onLoggedOut = onLoggedOut)
            }
        }
    }
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
