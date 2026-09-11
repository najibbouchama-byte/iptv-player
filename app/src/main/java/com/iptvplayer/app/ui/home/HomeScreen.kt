package com.iptvplayer.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Movie
import com.iptvplayer.app.ui.common.PosterCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onChannelClick: (Channel) -> Unit
) {
    val continueWatching by viewModel.continueWatching.collectAsState()
    val newReleases by viewModel.newReleases.collectAsState()
    val categoryRows by viewModel.categoryRows.collectAsState()
    val isLoading by viewModel.isLoadingDiscovery.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accueil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Image(
                    painter = painterResource(R.drawable.logo_infinity),
                    contentDescription = "Infinity Player",
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (continueWatching.isNotEmpty()) {
            item { SectionHeader("Reprendre la lecture") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(continueWatching, key = { it.channel.id }) { item ->
                        PosterCard(
                            title = item.channel.name,
                            posterUrl = item.channel.logoUrl,
                            showPoster = true,
                            progressFraction = item.progress,
                            onClick = { onChannelClick(item.channel) },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            if (newReleases.isNotEmpty()) {
                item { SectionHeader("Nouveautés") }
                item {
                    MovieRowContent(movies = newReleases, viewModel = viewModel, onChannelClick = onChannelClick)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            categoryRows.forEach { row ->
                item { SectionHeader(row.title) }
                item {
                    MovieRowContent(movies = row.movies, viewModel = viewModel, onChannelClick = onChannelClick)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun MovieRowContent(
    movies: List<Movie>,
    viewModel: HomeViewModel,
    onChannelClick: (Channel) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies, key = { it.id }) { movie ->
            PosterCard(
                title = movie.name,
                posterUrl = movie.posterUrl,
                showPoster = true,
                onClick = {
                    val url = viewModel.streamUrlFor(movie) ?: return@PosterCard
                    onChannelClick(
                        Channel(
                            id = movie.id,
                            name = movie.name,
                            logoUrl = movie.posterUrl,
                            streamUrl = url,
                            category = "Films",
                            epgChannelId = null
                        )
                    )
                },
                modifier = Modifier.width(120.dp)
            )
        }
    }
}
