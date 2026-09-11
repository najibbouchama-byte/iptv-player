package com.iptvplayer.app.ui.livetv

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel
@OptIn(ExperimentalMaterial3Api::class)
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

    var showCategorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

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

        // Bouton "Catégorie" façon TiviMate : ouvre un panneau qui glisse
        // depuis le bas avec la liste complète, plutôt qu'une rangée qui déborde.
        OutlinedButton(
            onClick = { showCategorySheet = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedCategory?.name ?: stringResource(R.string.live_tv_categories),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
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
                val listState = rememberLazyListState()
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
                            showLogo = true
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState
        ) {
            Text(
                text = stringResource(R.string.live_tv_categories),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(categories, key = { it.id }) { category ->
                    val isSelected = selectedCategory?.id == category.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectCategory(category)
                                showCategorySheet = false
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
