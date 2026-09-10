package com.iptvplayer.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.ui.favorites.FavoritesScreen
import com.iptvplayer.app.ui.home.HomeScreen
import com.iptvplayer.app.ui.livetv.LiveTvScreen
import com.iptvplayer.app.ui.movies.MoviesScreen
import com.iptvplayer.app.ui.player.PlayerScreen
import com.iptvplayer.app.ui.player.VlcPlayerScreen
import com.iptvplayer.app.ui.series.SeriesScreen
import com.iptvplayer.app.ui.settings.SettingsScreen

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("home", "Accueil", Icons.Filled.Home),
    Tab("live_tv", "Live TV", Icons.Filled.Tv),
    Tab("movies", "Films", Icons.Filled.Movie),
    Tab("series", "Séries", Icons.Filled.Theaters),
    Tab("favorites", "Favoris", Icons.Filled.Favorite),
    Tab("settings", "Paramètres", Icons.Filled.Settings)
)

@Composable
fun MainNavHost(onLoggedOut: () -> Unit) {
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }

    if (selectedChannel != null) {
        val channel = selectedChannel!!
        if (channel.category == "Films" || channel.category == "Séries") {
            VlcPlayerScreen(
                channel = channel,
                onBack = { selectedChannel = null }
            )
        } else {
            PlayerScreen(
                channel = channel,
                onBack = { selectedChannel = null }
            )
        }
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
                        label = { Text(tab.label) }
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
            composable("movies") {
                MoviesScreen(onChannelClick = { selectedChannel = it })
            }
            composable("series") {
                SeriesScreen(onChannelClick = { selectedChannel = it })
            }
            composable("favorites") {
                FavoritesScreen(onChannelClick = { selectedChannel = it })
            }
            composable("settings") {
                SettingsScreen(onLoggedOut = onLoggedOut)
            }
        }
    }
}
