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

    fun logout(onDone: () -> Unit) {
        securePrefs.clear()
        onDone()
    }
}
