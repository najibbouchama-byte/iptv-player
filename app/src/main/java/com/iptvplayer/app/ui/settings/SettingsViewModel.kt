package com.iptvplayer.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.repository.EpgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePrefs: SecurePrefs,
    private val epgRepository: EpgRepository
) : ViewModel() {

    private val _epgUrl = MutableStateFlow(securePrefs.epgUrl.orEmpty())
    val epgUrl: StateFlow<String> = _epgUrl

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _minBufferSeconds = MutableStateFlow(securePrefs.minBufferSeconds)
    val minBufferSeconds: StateFlow<Int> = _minBufferSeconds

    private val _maxBufferSeconds = MutableStateFlow(securePrefs.maxBufferSeconds)
    val maxBufferSeconds: StateFlow<Int> = _maxBufferSeconds

    val profileName: String get() = securePrefs.profileName.orEmpty()
    val playlistUrl: String get() = securePrefs.playlistUrl.orEmpty()

    fun updateEpgUrl(url: String) {
        _epgUrl.value = url
        securePrefs.epgUrl = url.trim()
    }

    fun syncEpgNow() {
        viewModelScope.launch {
            _syncing.value = true
            epgRepository.syncEpg()
            _syncing.value = false
        }
    }

    fun updateMinBuffer(seconds: Int) {
        _minBufferSeconds.value = seconds
        securePrefs.minBufferSeconds = seconds
        if (seconds > _maxBufferSeconds.value) {
            updateMaxBuffer(seconds)
        }
    }

    fun updateMaxBuffer(seconds: Int) {
        val safe = seconds.coerceAtLeast(_minBufferSeconds.value)
        _maxBufferSeconds.value = safe
        securePrefs.maxBufferSeconds = safe
    }

    fun logout(onDone: () -> Unit) {
        securePrefs.clear()
        onDone()
    }
}
