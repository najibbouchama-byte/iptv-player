package com.iptvplayer.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R
import com.iptvplayer.app.data.local.entity.MyListEntity
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.ui.common.PosterCard
import com.iptvplayer.app.ui.livetv.ChannelRow
import com.iptvplayer.app.ui.livetv.LiveTvViewModel

private enum class FavoritesTab { CHANNELS, MOVIES, SERIES }

@Composable
fun FavoritesScreen(
    liveTvViewModel: LiveTvViewModel = hiltViewModel(),
    myListViewModel: MyListViewModel = hiltViewModel(),
    onChannelClick: (List<Channel>, Int) -> Unit,
    onSeriesClick: (Series) -> Unit = {},
    onBrowseLiveTv: () -> Unit = {}
) {
    var tab by remember { mutableStateOf(FavoritesTab.CHANNELS) }

    val favorites by liveTvViewModel.favorites.collectAsState(initial = emptyList())
    val programsByChannel by liveTvViewModel.epgRepository.programsByChannel.collectAsState()
    val myMovies by myListViewModel.myMovies.collectAsState()
    val mySeries by myListViewModel.mySeries.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            FavTabOption("Chaînes", tab == FavoritesTab.CHANNELS, { tab = FavoritesTab.CHANNELS }, Modifier.weight(1f))
            FavTabOption("Films", tab == FavoritesTab.MOVIES, { tab = FavoritesTab.MOVIES }, Modifier.weight(1f))
            FavTabOption("Séries", tab == FavoritesTab.SERIES, { tab = FavoritesTab.SERIES }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (tab) {
            FavoritesTab.CHANNELS -> {
                if (favorites.isEmpty()) {
                    EmptyState(
                        message = stringResource(R.string.favorites_empty),
                        buttonLabel = "Parcourir Live TV",
                        icon = Icons.Filled.FavoriteBorder,
                        onAction = onBrowseLiveTv
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(favorites, key = { _, ch -> ch.id }) { index, channel ->
                            ChannelRow(
                                channel = channel,
                                isFavorite = true,
                                currentProgram = programsByChannel[channel.epgChannelId]
                                    ?.firstOrNull { it.isCurrent(System.currentTimeMillis()) },
                                onClick = { onChannelClick(favorites, index) },
                                onToggleFavorite = { liveTvViewModel.toggleFavorite(channel) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
            FavoritesTab.MOVIES -> {
                if (myMovies.isEmpty()) {
                    EmptyState(
                        message = "Aucun film dans ta liste.\nAppuie longuement sur une affiche dans Films pour l'ajouter.",
                        buttonLabel = null,
                        icon = Icons.Filled.FavoriteBorder,
                        onAction = null
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(myMovies, key = { it.itemId }) { entity ->
                            PosterCard(
                                title = entity.name,
                                posterUrl = entity.posterUrl,
                                showPoster = true,
                                onClick = {
                                    val url = entity.streamUrl ?: return@PosterCard
                                    onChannelClick(
                                        listOf(
                                            Channel(
                                                id = entity.itemId,
                                                name = entity.name,
                                                logoUrl = entity.posterUrl,
                                                streamUrl = url,
                                                category = "Films",
                                                epgChannelId = null
                                            )
                                        ),
                                        0
                                    )
                                },
                                onLongClick = { myListViewModel.remove(entity.itemId) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
            FavoritesTab.SERIES -> {
                if (mySeries.isEmpty()) {
                    EmptyState(
                        message = "Aucune série dans ta liste.\nAppuie longuement sur une affiche dans Séries pour l'ajouter.",
                        buttonLabel = null,
                        icon = Icons.Filled.FavoriteBorder,
                        onAction = null
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(mySeries, key = { it.itemId }) { entity ->
                            PosterCard(
                                title = entity.name,
                                posterUrl = entity.posterUrl,
                                showPoster = true,
                                onClick = {
                                    onSeriesClick(
                                        Series(
                                            id = entity.itemId,
                                            name = entity.name,
                                            posterUrl = entity.posterUrl,
                                            categoryId = entity.categoryId ?: "",
                                            plot = null
                                        )
                                    )
                                },
                                onLongClick = { myListViewModel.remove(entity.itemId) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavTabOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyState(
    message: String,
    buttonLabel: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: (() -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (buttonLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Icon(Icons.Filled.LiveTv, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonLabel)
            }
        }
    }
}
