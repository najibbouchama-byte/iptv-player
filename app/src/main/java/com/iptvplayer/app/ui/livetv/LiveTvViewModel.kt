package com.iptvplayer.app.ui.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.repository.EpgRepository
import com.iptvplayer.app.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    val epgRepository: EpgRepository
) : ViewModel() {

    val playlist = playlistRepository.playlist
    val favorites = playlistRepository.observeFavorites()
    val recentChannels = playlistRepository.observeRecent()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filteredChannels(allChannels: List<Channel>): List<Channel> {
        val category = _selectedCategory.value
        val query = _searchQuery.value.trim().lowercase()
        return allChannels.filter { channel ->
            (category == null || channel.category == category) &&
                (query.isEmpty() || channel.name.lowercase().contains(query))
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
