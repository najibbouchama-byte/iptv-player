package com.iptvplayer.app.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Episode
import com.iptvplayer.app.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val xtreamRepository: XtreamRepository
) : ViewModel() {

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var loadedSeriesId: String? = null

    fun loadEpisodes(seriesId: String) {
        if (loadedSeriesId == seriesId) return
        loadedSeriesId = seriesId
        viewModelScope.launch {
            _isLoading.value = true
            _episodes.value = xtreamRepository.getSeriesEpisodes(seriesId)
            _isLoading.value = false
        }
    }

    fun streamUrlFor(episode: Episode): String? = xtreamRepository.episodeStreamUrl(episode)
}
