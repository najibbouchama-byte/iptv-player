package com.iptvplayer.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Movie
import com.iptvplayer.app.data.repository.WatchProgressRepository
import com.iptvplayer.app.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ContinueWatchingItem(val channel: Channel, val progress: Float)
data class MovieRow(val title: String, val movies: List<Movie>)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val xtreamRepository: XtreamRepository,
    private val watchProgressRepository: WatchProgressRepository
) : ViewModel() {

    val continueWatching: StateFlow<List<ContinueWatchingItem>> =
        watchProgressRepository.observeContinueWatching()
            .map { list ->
                list.map { entity ->
                    val fraction = if (entity.durationMs > 0) entity.positionMs.toFloat() / entity.durationMs else 0f
                    ContinueWatchingItem(watchProgressRepository.toChannel(entity), fraction)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _newReleases = MutableStateFlow<List<Movie>>(emptyList())
    val newReleases: StateFlow<List<Movie>> = _newReleases

    private val _categoryRows = MutableStateFlow<List<MovieRow>>(emptyList())
    val categoryRows: StateFlow<List<MovieRow>> = _categoryRows

    private val _liveChannels = MutableStateFlow<List<Channel>>(emptyList())
    val liveChannels: StateFlow<List<Channel>> = _liveChannels

    private val _isLoadingDiscovery = MutableStateFlow(true)
    val isLoadingDiscovery: StateFlow<Boolean> = _isLoadingDiscovery

    init {
        viewModelScope.launch { loadDiscoveryContent() }
    }

    private suspend fun loadDiscoveryContent() {
        _isLoadingDiscovery.value = true
        try {
            xtreamRepository.loadVodCategoriesIfNeeded()
            val vodCategories = xtreamRepository.vodCategories.value.take(3)

            val rows = mutableListOf<MovieRow>()
            val allMovies = mutableListOf<Movie>()
            for (category in vodCategories) {
                val movies = xtreamRepository.getVodStreams(category.id)
                allMovies += movies
                if (movies.isNotEmpty()) {
                    rows += MovieRow(category.name, movies.take(12))
                }
            }
            _categoryRows.value = rows

            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
            val recentOnes = allMovies.filter { it.name.contains(currentYear) }
            _newReleases.value = (recentOnes.ifEmpty { allMovies }).take(12)

            val liveCategories = xtreamRepository.liveCategories.value
            if (liveCategories.isNotEmpty()) {
                _liveChannels.value = xtreamRepository.getLiveStreams(liveCategories.first().id).take(12)
            }
        } finally {
            _isLoadingDiscovery.value = false
        }
    }

    fun streamUrlFor(movie: Movie): String? = xtreamRepository.movieStreamUrl(movie)
}
