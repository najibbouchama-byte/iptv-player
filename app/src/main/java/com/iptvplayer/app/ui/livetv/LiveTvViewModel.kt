package com.iptvplayer.app.ui.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.XtreamCategory
import com.iptvplayer.app.data.repository.EpgRepository
import com.iptvplayer.app.data.repository.PlaylistRepository
import com.iptvplayer.app.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    }

    fun filteredChannels(): List<Channel> {
        val query = _searchQuery.value.trim().lowercase()
        val all = _channels.value
        return if (query.isEmpty()) all else all.filter { it.name.lowercase().contains(query) }
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
