package com.iptvplayer.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.model.XtreamAuthResult
import com.iptvplayer.app.data.network.XtreamCredentials
import com.iptvplayer.app.data.repository.XtreamRepository
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
    private val xtreamRepository: XtreamRepository,
    private val securePrefs: SecurePrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _isM3uMode = MutableStateFlow(false)
    val isM3uMode: StateFlow<Boolean> = _isM3uMode.asStateFlow()

    fun toggleMode() {
        _isM3uMode.value = !_isM3uMode.value
        _uiState.value = LoginUiState.Idle
    }

    fun connect(
        profileName: String,
        serverUrl: String,
        username: String,
        password: String,
        epgUrl: String,
        rememberMe: Boolean
    ) {
        val trimmedUrl = serverUrl.trim().trimEnd('/')

        if (trimmedUrl.isEmpty()) {
            _uiState.value = LoginUiState.Error("EMPTY_URL")
            return
        }
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            _uiState.value = LoginUiState.Error("INVALID_URL")
            return
        }

        val credentials: XtreamCredentials

        if (_isM3uMode.value) {
            val parsed = XtreamCredentials.parse(trimmedUrl)
            if (parsed == null) {
                _uiState.value = LoginUiState.Error("INVALID_M3U_LINK")
                return
            }
            credentials = parsed
        } else {
            if (username.isBlank() || password.isBlank()) {
                _uiState.value = LoginUiState.Error("MISSING_CREDENTIALS")
                return
            }
            credentials = XtreamCredentials(trimmedUrl, username.trim(), password.trim())
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                when (val result = xtreamRepository.connect(credentials)) {
                    is XtreamAuthResult.Success -> {
                        if (rememberMe) {
                            securePrefs.xtreamServerUrl = credentials.baseUrl
                            securePrefs.xtreamUsername = credentials.username
                            securePrefs.xtreamPassword = credentials.password
                            securePrefs.playlistUrl = credentials.baseUrl
                            securePrefs.profileName = profileName.trim()
                            securePrefs.epgUrl = epgUrl.trim()
                            securePrefs.connectionMode =
                                if (_isM3uMode.value) SecurePrefs.MODE_M3U else SecurePrefs.MODE_XTREAM
                            securePrefs.rememberMe = true
                        }
                        _uiState.value = LoginUiState.Success
                    }
                    is XtreamAuthResult.Failure -> {
                        _uiState.value = LoginUiState.Error(result.reason)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("NETWORK_ERROR")
            }
        }
    }
}
