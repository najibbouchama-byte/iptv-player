package com.iptvplayer.app.ui.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.iptvplayer.app.data.model.Channel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import javax.inject.Inject

@HiltViewModel
class VlcPlayerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val libVLC = LibVLC(application, arrayListOf("--no-drop-late-frames", "--no-skip-frames"))
    val mediaPlayer = MediaPlayer(libVLC)

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isBuffering = MutableStateFlow(true)
    val isBuffering: StateFlow<Boolean> = _isBuffering

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val aspectModes = listOf(null, "16:9", "4:3", "1:1")
    private var aspectIndex = 0
    private val _aspectLabel = MutableStateFlow("Ajusté")
    val aspectLabel: StateFlow<String> = _aspectLabel

    init {
        mediaPlayer.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Playing -> {
                    _isPlaying.value = true
                    _isBuffering.value = false
                    _errorMessage.value = null
                }
                MediaPlayer.Event.Paused -> _isPlaying.value = false
                MediaPlayer.Event.Buffering -> {
                    _isBuffering.value = event.buffering < 100f
                }
                MediaPlayer.Event.TimeChanged -> {
                    _currentPosition.value = event.timeChanged
                    _duration.value = mediaPlayer.length
                }
                MediaPlayer.Event.EncounteredError -> {
                    _isBuffering.value = false
                    _errorMessage.value = "Le lecteur de secours n'a pas réussi à lire ce contenu non plus."
                }
                MediaPlayer.Event.EndReached -> {
                    _isPlaying.value = false
                }
            }
        }
    }

    fun seekRelative(deltaMs: Long) {
        val length = mediaPlayer.length
        if (length <= 0) return
        val newPosition = (mediaPlayer.time + deltaMs).coerceIn(0, length)
        mediaPlayer.time = newPosition
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer.time = positionMs
    }

    fun cycleAspectRatio() {
        aspectIndex = (aspectIndex + 1) % aspectModes.size
        val mode = aspectModes[aspectIndex]
        mediaPlayer.setAspectRatio(mode)
        _aspectLabel.value = mode ?: "Ajusté"
    }

    fun play(channel: Channel) {
        _errorMessage.value = null
        _isBuffering.value = true
        _currentChannel.value = channel
        val media = Media(libVLC, Uri.parse(channel.streamUrl))
        media.setHWDecoderEnabled(false, false)
        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()
    }

    fun togglePlayPause() {
        if (mediaPlayer.isPlaying) mediaPlayer.pause() else mediaPlayer.play()
    }

    override fun onCleared() {
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        mediaPlayer.release()
        libVLC.release()
        super.onCleared()
    }
}
