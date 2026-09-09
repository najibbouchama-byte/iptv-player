package com.iptvplayer.app.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.ui.livetv.ChannelRow
import com.iptvplayer.app.ui.livetv.LiveTvViewModel

@Composable
fun FavoritesScreen(
    viewModel: LiveTvViewModel = hiltViewModel(),
    onChannelClick: (Channel) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())
    val programsByChannel by viewModel.epgRepository.programsByChannel.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.favorites_empty),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(favorites, key = { it.id }) { channel ->
                    ChannelRow(
                        channel = channel,
                        isFavorite = true,
                        currentProgram = programsByChannel[channel.epgChannelId]
                            ?.firstOrNull { it.isCurrent(System.currentTimeMillis()) },
                        onClick = { onChannelClick(channel) },
                        onToggleFavorite = { viewModel.toggleFavorite(channel) }
                    )
                }
            }
        }
    }
}
