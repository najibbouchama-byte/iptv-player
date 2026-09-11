package com.iptvplayer.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
    Tab("settings", "Réglages", Icons.Filled.Settings)
)

@Composable
fun MainNavHost(onLoggedOut: () -> Unit) {
    var playerQueue by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var playerIndex by remember { mutableStateOf(0) }

    val navController = rememberNavController()

    if (playerQueue.isNotEmpty()) {
        val channel = playerQueue[playerIndex]
        val hasNext = playerIndex < playerQueue.size - 1
        val hasPrevious = playerIndex > 0
        if (channel.category == "Films" || channel.category == "Séries") {
            VlcPlayerScreen(
                channel = channel,
                hasNext = hasNext,
                hasPrevious = hasPrevious,
                onNext = { if (hasNext) playerIndex++ },
                onPrevious = { if (hasPrevious) playerIndex-- },
                onBack = { playerQueue = emptyList(); playerIndex = 0 }
            )
        } else {
            PlayerScreen(
                channel = channel,
                onBack = { playerQueue = emptyList(); playerIndex = 0 }
            )
        }
        return
    }

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
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp.let { it })
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(onChannelClick = { playerQueue = listOf(it); playerIndex = 0 })
            }
            composable("live_tv") {
                LiveTvScreen(onChannelClick = { playerQueue = listOf(it); playerIndex = 0 })
            }
            composable("movies") {
                MoviesScreen(onChannelClick = { playerQueue = listOf(it); playerIndex = 0 })
            }
            composable("series") {
                SeriesScreen(onPlayEpisodes = { channels, startIndex ->
                    playerQueue = channels
                    playerIndex = startIndex
                })
            }
            composable("favorites") {
                FavoritesScreen(onChannelClick = { playerQueue = listOf(it); playerIndex = 0 })
            }
            composable("settings") {
                SettingsScreen(onLoggedOut = onLoggedOut)
            }
        }
    }
}
