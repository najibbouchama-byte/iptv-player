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
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

data class ContinueWatchingItem(val channel: Channel, val progress: Float)
data class MovieRow(val title: String, val movies: List<Movie>)

private data class GenreDefinition(val label: String, val keywords: List<String>)

private val GENRES = listOf(
    GenreDefinition("Action", listOf("action")),
    GenreDefinition("Comédie", listOf("comedie", "comedy")),
    GenreDefinition("Drame", listOf("drame", "drama")),
    GenreDefinition("Thriller", listOf("thriller", "suspense")),
    GenreDefinition("Science-Fiction", listOf("science fiction", "sci-fi", "scifi")),
    GenreDefinition("Horreur", listOf("horreur", "horror")),
    GenreDefinition("Animation", listOf("animation", "anime", "dessin anime", "dessins animes")),
    GenreDefinition("Aventure", listOf("aventure", "adventure")),
    GenreDefinition("Fantastique", listOf("fantastique", "fantasy")),
    GenreDefinition("Policier", listOf("policier", "crime")),
    GenreDefinition("Romance", listOf("romance", "romantique")),
    GenreDefinition("Guerre", listOf("guerre")),
    GenreDefinition("Documentaire", listOf("documentaire", "docu"))
)

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

    private val _topTen = MutableStateFlow<List<Movie>>(emptyList())
    val topTen: StateFlow<List<Movie>> = _topTen

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
            val allMovies = loadAllMoviesIndexed()

            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
            val recentOnes = allMovies.filter { it.name.contains(currentYear) }
            _newReleases.value = (recentOnes.ifEmpty { allMovies }).take(12)

            // Top 10 façon Netflix : sélection qui change tous les 2 jours,
            // calculée à partir de la date du jour, sans avoir besoin d'un serveur.
            if (allMovies.isNotEmpty()) {
                val epochDay = System.currentTimeMillis() / (1000L * 60 * 60 * 24)
                val seed = epochDay / 2
                _topTen.value = allMovies.shuffled(Random(seed)).take(10)
            }

            // Sections par genre, déduites du nom des catégories du panel
            val rows = mutableListOf<MovieRow>()
            val vodCategories = xtreamRepository.vodCategories.value
            for (genre in GENRES) {
                val matchingCategoryIds = vodCategories
                    .filter { category -> genre.keywords.any { normalize(category.name).contains(it) } }
                    .map { it.id }
                    .toSet()
                if (matchingCategoryIds.isEmpty()) continue

                val genreMovies = allMovies.filter { it.categoryId in matchingCategoryIds }
                    .distinctBy { it.id }
                if (genreMovies.isNotEmpty()) {
                    rows += MovieRow(genre.label, genreMovies.take(15))
                }
            }
            _categoryRows.value = rows
        } finally {
            _isLoadingDiscovery.value = false
        }
    }

    /**
     * Charge TOUS les films de TOUTES les catégories, en parallèle (par lots
     * de 6 requêtes), une seule fois. Sert à la fois aux sections par genre,
     * au Top 10, et à la recherche (qui devient alors quasi instantanée).
     */
    private suspend fun loadAllMoviesIndexed(): List<Movie> {
        allMoviesIndex?.let { return it }
        xtreamRepository.loadVodCategoriesIfNeeded()
        val semaphore = Semaphore(6)
        val movies = coroutineScope {
            xtreamRepository.vodCategories.value.map { category ->
                async { semaphore.withPermit { xtreamRepository.getVodStreams(category.id) } }
            }.awaitAll().flatten()
        }
        allMoviesIndex = movies
        return movies
    }

    private suspend fun ensureSeriesIndexLoaded() {
        if (allSeriesIndex != null) return
        xtreamRepository.loadSeriesCategoriesIfNeeded()
        val semaphore = Semaphore(6)
        allSeriesIndex = coroutineScope {
            xtreamRepository.seriesCategories.value.map { category ->
                async { semaphore.withPermit { xtreamRepository.getSeries(category.id) } }
            }.awaitAll().flatten()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchMovies.value = emptyList()
            _searchSeries.value = emptyList()
            _isIndexing.value = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _isIndexing.value = true
            try {
                val movies = loadAllMoviesIndexed()
                ensureSeriesIndexLoaded()
                val q = query.trim()
                _searchMovies.value = movies.filter { it.name.contains(q, ignoreCase = true) }.take(30)
                _searchSeries.value = allSeriesIndex.orEmpty()
                    .filter { it.name.contains(q, ignoreCase = true) }
                    .take(30)
            } finally {
                _isIndexing.value = false
            }
        }
    }

    private fun normalize(name: String): String {
        val withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutAccents.lowercase(Locale.FRENCH)
    }

    fun streamUrlFor(movie: Movie): String? = xtreamRepository.movieStreamUrl(movie)
}
