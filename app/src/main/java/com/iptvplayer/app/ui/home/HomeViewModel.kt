package com.iptvplayer.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Movie
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.repository.WatchProgressRepository
import com.iptvplayer.app.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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

    private val _isLoadingDiscovery = MutableStateFlow(true)
    val isLoadingDiscovery: StateFlow<Boolean> = _isLoadingDiscovery

    // --- Recherche ---

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isIndexing = MutableStateFlow(false)
    val isIndexing: StateFlow<Boolean> = _isIndexing

    private val _searchMovies = MutableStateFlow<List<Movie>>(emptyList())
    val searchMovies: StateFlow<List<Movie>> = _searchMovies

    private val _searchSeries = MutableStateFlow<List<Series>>(emptyList())
    val searchSeries: StateFlow<List<Series>> = _searchSeries

    private var allMoviesIndex: List<Movie>? = null
    private var allSeriesIndex: List<Series>? = null
    private var searchJob: Job? = null

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
        } finally {
            _isLoadingDiscovery.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchMovies.value = emptyList()
            _searchSeries.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            ensureFullIndexLoaded()
            val q = query.trim()
            _searchMovies.value = allMoviesIndex.orEmpty()
                .filter { it.name.contains(q, ignoreCase = true) }
                .take(30)
            _searchSeries.value = allSeriesIndex.orEmpty()
                .filter { it.name.contains(q, ignoreCase = true) }
                .take(30)
        }
    }

    /**
     * Charge tout le catalogue (films + séries, toutes catégories) UNE seule fois,
     * en lançant les requêtes réseau en parallèle (par lots de 6) plutôt que les
     * unes après les autres, pour que la première recherche soit rapide.
     */
    private suspend fun ensureFullIndexLoaded() {
        if (allMoviesIndex != null && allSeriesIndex != null) return
        _isIndexing.value = true
        try {
            xtreamRepository.loadVodCategoriesIfNeeded()
            xtreamRepository.loadSeriesCategoriesIfNeeded()

            val semaphore = Semaphore(6)

            coroutineScope {
                val movieDeferreds = xtreamRepository.vodCategories.value.map { category ->
                    async { semaphore.withPermit { xtreamRepository.getVodStreams(category.id) } }
                }
                val seriesDeferreds = xtreamRepository.seriesCategories.value.map { category ->
                    async { semaphore.withPermit { xtreamRepository.getSeries(category.id) } }
                }
                allMoviesIndex = movieDeferreds.awaitAll().flatten()
                allSeriesIndex = seriesDeferreds.awaitAll().flatten()
            }
        } finally {
            _isIndexing.value = false
        }
    }

    fun streamUrlFor(movie: Movie): String? = xtreamRepository.movieStreamUrl(movie)
}
