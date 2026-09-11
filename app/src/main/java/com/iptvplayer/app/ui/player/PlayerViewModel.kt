package com.iptvplayer.app.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.EpgProgram
import com.iptvplayer.app.data.repository.EpgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val epgRepository: EpgRepository,
    securePrefs: SecurePrefs
) : AndroidViewModel(application) {

    private val renderersFactory = DefaultRenderersFactory(application)
        .setEnableDecoderFallback(true)

    private val loadControl = run {
        val minBufferMs = (securePrefs.minBufferSeconds * 1000).coerceAtLeast(1000)
        val maxBufferMs = (securePrefs.maxBufferSeconds * 1000).coerceAtLeast(minBufferMs)
        val playbackMs = minBufferMs.coerceAtMost(2000)
        val playbackAfterRebufferMs = minBufferMs.coerceAtMost(5000)
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(minBufferMs, maxBufferMs, playbackMs, playbackAfterRebufferMs)
            .build()
    }

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application, renderersFactory)
        .setLoadControl(loadControl)
        .build()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isBuffering = MutableStateFlow(true)
    val isBuffering: StateFlow<Boolean> = _isBuffering

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    _errorMessage.value = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isBuffering.value = false
                _errorMessage.value = error.errorCodeName + " : " + (error.cause?.message ?: error.message ?: "Erreur inconnue")
            }
        })
    }

    fun playChannel(channel: Channel) {
        _errorMessage.value = null
        _isBuffering.value = true
        _currentChannel.value = channel
        exoPlayer.setMediaItem(MediaItem.fromUri(channel.streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun togglePlayPause() {
        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
    }

    fun currentProgramTitle(): String? =
        epgRepository.currentProgram(_currentChannel.value?.epgChannelId)?.title

    fun programFor(epgChannelId: String?): EpgProgram? =
        epgRepository.currentProgram(epgChannelId)

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}
