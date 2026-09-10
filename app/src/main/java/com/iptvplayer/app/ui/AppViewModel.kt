package com.iptvplayer.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.repository.EpgRepository
import com.iptvplayer.app.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SessionState {
    data object Checking : SessionState
    data object LoggedOut : SessionState
    data object LoggedIn : SessionState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val securePrefs: SecurePrefs,
    private val xtreamRepository: XtreamRepository,
    private val epgRepository: EpgRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Checking)
    val sessionState: StateFlow<SessionState> = _sessionState

    init {
        viewModelScope.launch {
            if (securePrefs.isLoggedIn()) {
                try {
                    val connected = xtreamRepository.reconnectFromSavedUrl()
                    if (connected) {
                        epgRepository.syncEpg()
                        _sessionState.value = SessionState.LoggedIn
                    } else {
                        _sessionState.value = SessionState.LoggedOut
                    }
                } catch (e: Exception) {
                    _sessionState.value = SessionState.LoggedOut
                }
            } else {
                _sessionState.value = SessionState.LoggedOut
            }
        }
    }

    fun markLoggedIn() {
        _sessionState.value = SessionState.LoggedIn
        viewModelScope.launch { epgRepository.syncEpg() }
    }

    fun markLoggedOut() {
        _sessionState.value = SessionState.LoggedOut
    }
}
