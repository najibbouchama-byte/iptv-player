package com.iptvplayer.app.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.ui.livetv.ChannelRow
import com.iptvplayer.app.ui.livetv.LiveTvViewModel

@Composable
fun SearchScreen(
    viewModel: LiveTvViewModel = hiltViewModel(),
    onChannelClick: (Channel) -> Unit
) {
    val playlist by viewModel.playlist.collectAsState()
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }

    val allChannels = playlist?.channels.orEmpty()
    val favoriteIds = favorites.map { it.id }.toSet()
    val results = if (query.isBlank()) emptyList() else allChannels.filter {
        it.name.lowercase().contains(query.lowercase())
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.search_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(stringResource(R.string.live_tv_search_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results, key = { it.id }) { channel ->
                ChannelRow(
                    channel = channel,
                    isFavorite = favoriteIds.contains(channel.id),
                    currentProgram = null,
                    onClick = { onChannelClick(channel) },
                    onToggleFavorite = { viewModel.toggleFavorite(channel) }
                )
            }
        }
    }
}
