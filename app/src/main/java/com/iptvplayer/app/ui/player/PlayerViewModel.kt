package com.iptvplayer.app.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.repository.EpgRepository
import com.iptvplayer.app.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val playlistRepository: PlaylistRepository,
    private val epgRepository: EpgRepository
) : AndroidViewModel(application) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    /** Change la chaîne actuellement lue dans le player. */
    fun playChannel(channel: Channel) {
        _currentChannel.value = channel
        exoPlayer.setMediaItem(MediaItem.fromUri(channel.streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun togglePlayPause() {
        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
    }

    /** Passe à la chaîne suivante/précédente dans la playlist complète (même ordre que l'écran Live TV). */
    fun switchChannel(direction: Int) {
        val all = playlistRepository.playlist.value?.channels ?: return
        val current = _currentChannel.value ?: return
        val currentIndex = all.indexOfFirst { it.id == current.id }
        if (currentIndex == -1) return
        val newIndex = (currentIndex + direction + all.size) % all.size
        playChannel(all[newIndex])
    }

    fun currentProgramTitle(): String? =
        epgRepository.currentProgram(_currentChannel.value?.epgChannelId)?.title

    fun nextProgramTitle(): String? =
        epgRepository.nextProgram(_currentChannel.value?.epgChannelId)?.title

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}
