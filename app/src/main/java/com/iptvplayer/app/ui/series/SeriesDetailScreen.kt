package com.iptvplayer.app.ui.series

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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

    BackHandler(onBack = onBack)

    LaunchedEffect(series.id) {
        viewModel.loadEpisodes(series.id)
    }

    val seasons = remember(episodes) { episodes.map { it.seasonNumber }.distinct().sorted() }
    var selectedSeason by remember(seasons) { mutableStateOf(seasons.firstOrNull() ?: 1) }
    val seasonEpisodes = remember(episodes, selectedSeason) {
        episodes.filter { it.seasonNumber == selectedSeason }.sortedBy { it.episodeNumber }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            if (!series.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = series.posterUrl,
                    contentDescription = series.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.35f),
                                0.55f to Color.Black.copy(alpha = 0.55f),
                                1.0f to MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text(
                text = series.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            series.plot?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (episodes.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
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

                if (seasons.size > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(seasons) { season ->
                            FilterChip(
                                selected = season == selectedSeason,
                                onClick = { selectedSeason = season },
                                label = { Text("Saison $season") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    seasonEpisodes.forEach { episode ->
                        val fullIndex = episodes.indexOf(episode)
                        EpisodeRow(
                            episode = episode,
                            onClick = { onPlayEpisodes(channels, fullIndex) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Les panels Xtream collent souvent le nom de la série + le code saison/épisode
 * dans le titre brut (ex: "Dexter (2025) - S01E01 - A Beating Heart").
 * On ne garde que la partie utile après le code SxxExx.
 */
private fun cleanEpisodeTitle(rawTitle: String): String {
    val match = Regex("S\\d{1,2}\\s*E\\d{1,3}", RegexOption.IGNORE_CASE).find(rawTitle)
    val after = if (match != null) rawTitle.substring(match.range.last + 1) else rawTitle
    return after.trim(' ', '-', ':', '.').ifBlank { rawTitle }
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
        Text(
            text = "Épisode ${episode.episodeNumber} : ${cleanEpisodeTitle(episode.title)}",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}
