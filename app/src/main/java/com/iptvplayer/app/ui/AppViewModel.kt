package com.iptvplayer.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.repository.EpgRepository
import com.iptvplayer.app.data.repository.PlaylistRepository
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
    private val playlistRepository: PlaylistRepository,
    private val epgRepository: EpgRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Checking)
    val sessionState: StateFlow<SessionState> = _sessionState

    init {
        viewModelScope.launch {
            if (securePrefs.isLoggedIn()) {
                try {
                    playlistRepository.reloadSavedPlaylist()
                    epgRepository.syncEpg()
                    _sessionState.value = SessionState.LoggedIn
                } catch (e: Exception) {
                    // La playlist enregistrée est peut-être devenue invalide : on renvoie vers la connexion
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
