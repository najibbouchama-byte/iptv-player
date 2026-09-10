package com.iptvplayer.app.ui.livetv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel

@Composable
fun LiveTvScreen(
    viewModel: LiveTvViewModel = hiltViewModel(),
    onChannelClick: (Channel) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoadingChannels by viewModel.isLoadingChannels.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val programsByChannel by viewModel.epgRepository.programsByChannel.collectAsState()

    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }
    val filteredChannels = remember(channels, searchQuery) { viewModel.filteredChannels() }

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            viewModel.selectCategory(categories.first())
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.live_tv_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            placeholder = { Text(stringResource(R.string.live_tv_search_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { category ->
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoadingChannels -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            filteredChannels.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.live_tv_no_channels),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            else -> {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredChannels, key = { it.id }) { channel ->
                        ChannelRow(
                            channel = channel,
                            isFavorite = favoriteIds.contains(channel.id),
                            currentProgram = programsByChannel[channel.epgChannelId]
                                ?.firstOrNull { it.isCurrent(System.currentTimeMillis()) },
                            onClick = {
                                viewModel.recordWatched(channel)
                                onChannelClick(channel)
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(channel) },
                            showLogo = !listState.isScrollInProgress
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}
