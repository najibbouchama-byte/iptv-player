package com.iptvplayer.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.entity.MyListEntity
import com.iptvplayer.app.data.repository.MyListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val myListRepository: MyListRepository
) : ViewModel() {

    val myMovies: StateFlow<List<MyListEntity>> = myListRepository.observeByType("movie")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mySeries: StateFlow<List<MyListEntity>> = myListRepository.observeByType("series")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remove(itemId: String) {
        viewModelScope.launch { myListRepository.remove(itemId) }
    }
}
