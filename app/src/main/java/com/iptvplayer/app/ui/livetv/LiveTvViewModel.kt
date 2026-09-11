package com.iptvplayer.app.ui.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.XtreamCategory
import com.iptvplayer.app.data.repository.EpgRepository
import com.iptvplayer.app.data.repository.PlaylistRepository
import com.iptvplayer.app.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val xtreamRepository: XtreamRepository,
    private val playlistRepository: PlaylistRepository,
    val epgRepository: EpgRepository
) : ViewModel() {

    val categories = xtreamRepository.liveCategories
    val favorites = playlistRepository.observeFavorites()
    val recentChannels = playlistRepository.observeRecent()

    private val _selectedCategory = MutableStateFlow<XtreamCategory?>(null)
    val selectedCategory: StateFlow<XtreamCategory?> = _selectedCategory.asStateFlow()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _isLoadingChannels = MutableStateFlow(false)
    val isLoadingChannels: StateFlow<Boolean> = _isLoadingChannels.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isIndexingChannels = MutableStateFlow(false)
    val isIndexingChannels: StateFlow<Boolean> = _isIndexingChannels.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Channel>>(emptyList())
    val searchResults: StateFlow<List<Channel>> = _searchResults.asStateFlow()

    private var allChannelsIndex: List<Channel>? = null
    private var searchJob: Job? = null

    fun selectCategory(category: XtreamCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _isLoadingChannels.value = true
            _channels.value = xtreamRepository.getLiveStreams(category.id)
            _isLoadingChannels.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(250)
            ensureFullChannelIndexLoaded()
            val q = query.trim().lowercase()
            _searchResults.value = allChannelsIndex.orEmpty()
                .filter { it.name.lowercase().contains(q) }
                .take(60)
        }
    }

    /**
     * Charge TOUTES les chaînes de TOUTES les catégories une seule fois, en
     * parallèle (par lots de 6 requêtes), pour que la recherche porte sur
     * l'intégralité des chaînes et pas seulement la catégorie affichée à l'écran.
     */
    private suspend fun ensureFullChannelIndexLoaded() {
        if (allChannelsIndex != null) return
        _isIndexingChannels.value = true
        try {
            val semaphore = Semaphore(6)
            coroutineScope {
                val deferreds = categories.value.map { category ->
                    async { semaphore.withPermit { xtreamRepository.getLiveStreams(category.id) } }
                }
                allChannelsIndex = deferreds.awaitAll().flatten()
            }
        } finally {
            _isIndexingChannels.value = false
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            playlistRepository.toggleFavorite(channel)
        }
    }

    fun recordWatched(channel: Channel) {
        viewModelScope.launch {
            playlistRepository.recordWatched(channel)
        }
    }
}
