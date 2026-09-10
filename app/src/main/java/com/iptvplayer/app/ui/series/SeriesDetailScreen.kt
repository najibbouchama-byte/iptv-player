package com.iptvplayer.app.ui.series

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Episode
import com.iptvplayer.app.data.model.Series

@Composable
fun SeriesDetailScreen(
    series: Series,
    viewModel: SeriesDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPlayEpisodes: (List<Channel>, Int) -> Unit
) {
    val episodes by viewModel.episodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(series.id) {
        viewModel.loadEpisodes(series.id)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
            }
            Text(series.name, style = MaterialTheme.typography.titleLarge)
        }
        series.plot?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (episodes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun épisode disponible", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            val channels = remember(episodes) {
                episodes.map { episode ->
                    Channel(
                        id = episode.id,
                        name = "S${episode.seasonNumber} E${episode.episodeNumber} — ${episode.title}",
                        logoUrl = null,
                        streamUrl = viewModel.streamUrlFor(episode) ?: "",
                        category = "Séries",
                        epgChannelId = null
                    )
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(episodes.size, key = { episodes[it].id }) { index ->
                    EpisodeRow(episode = episodes[index], onClick = {
                        onPlayEpisodes(channels, index)
                    })
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Saison ${episode.seasonNumber} · Épisode ${episode.episodeNumber}", style = MaterialTheme.typography.labelMedium)
            Text(episode.title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
