package com.iptvplayer.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
    data object Success : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val securePrefs: SecurePrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun connect(url: String, profileName: String, rememberMe: Boolean) {
        val trimmedUrl = url.trim()

        if (trimmedUrl.isEmpty()) {
            _uiState.value = LoginUiState.Error("EMPTY_URL")
            return
        }
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            _uiState.value = LoginUiState.Error("INVALID_URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                playlistRepository.loadPlaylist(trimmedUrl)

                // On enregistre les informations UNIQUEMENT si l'utilisateur a coché "se souvenir de moi",
                // et toujours dans le stockage chiffré (jamais en clair).
                if (rememberMe) {
                    securePrefs.playlistUrl = trimmedUrl
                    securePrefs.profileName = profileName.trim()
                    securePrefs.rememberMe = true
                }
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("NETWORK_ERROR")
            }
        }
    }
}
