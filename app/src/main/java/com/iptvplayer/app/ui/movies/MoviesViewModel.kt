package com.iptvplayer.app.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Movie
import com.iptvplayer.app.data.model.XtreamCategory
import com.iptvplayer.app.data.repository.MyListRepository
import com.iptvplayer.app.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val xtreamRepository: XtreamRepository,
    private val myListRepository: MyListRepository
) : ViewModel() {

    val categories = xtreamRepository.vodCategories

    private val _selectedCategory = MutableStateFlow<XtreamCategory?>(null)
    val selectedCategory: StateFlow<XtreamCategory?> = _selectedCategory.asStateFlow()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val myListIds: StateFlow<Set<String>> = myListRepository.observeByType("movie")
        .map { list -> list.map { it.itemId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        viewModelScope.launch { xtreamRepository.loadVodCategoriesIfNeeded() }
    }

    fun selectCategory(category: XtreamCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _isLoading.value = true
            _movies.value = xtreamRepository.getVodStreams(category.id)
            _isLoading.value = false
        }
    }

    fun toggleMyList(movie: Movie) {
        viewModelScope.launch {
            myListRepository.toggle(
                itemId = movie.id,
                type = "movie",
                name = movie.name,
                posterUrl = movie.posterUrl,
                streamUrl = streamUrlFor(movie),
                categoryId = movie.categoryId
            )
        }
    }

    fun streamUrlFor(movie: Movie): String? = xtreamRepository.movieStreamUrl(movie)
}
