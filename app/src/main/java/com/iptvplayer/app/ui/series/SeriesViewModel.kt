package com.iptvplayer.app.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Series
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
class SeriesViewModel @Inject constructor(
    private val xtreamRepository: XtreamRepository,
    private val myListRepository: MyListRepository
) : ViewModel() {

    val categories = xtreamRepository.seriesCategories

    private val _selectedCategory = MutableStateFlow<XtreamCategory?>(null)
    val selectedCategory: StateFlow<XtreamCategory?> = _selectedCategory.asStateFlow()

    private val _series = MutableStateFlow<List<Series>>(emptyList())
    val series: StateFlow<List<Series>> = _series.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedSeriesId = MutableStateFlow<String?>(null)
    val selectedSeriesId: StateFlow<String?> = _selectedSeriesId.asStateFlow()

    val myListIds: StateFlow<Set<String>> = myListRepository.observeByType("series")
        .map { list -> list.map { it.itemId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun selectSeries(id: String?) {
        _selectedSeriesId.value = id
    }

    init {
        viewModelScope.launch { xtreamRepository.loadSeriesCategoriesIfNeeded() }
    }

    fun selectCategory(category: XtreamCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _isLoading.value = true
            _series.value = xtreamRepository.getSeries(category.id)
            _isLoading.value = false
        }
    }

    fun toggleMyList(series: Series) {
        viewModelScope.launch {
            myListRepository.toggle(
                itemId = series.id,
                type = "series",
                name = series.name,
                posterUrl = series.posterUrl,
                streamUrl = null,
                categoryId = series.categoryId
            )
        }
    }
}
