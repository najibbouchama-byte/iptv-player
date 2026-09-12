package com.iptvplayer.app.ui.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.repository.WatchProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import javax.inject.Inject

data class TrackOption(val id: Int, val name: String)

@HiltViewModel
class VlcPlayerViewModel @Inject constructor(
    application: Application,
    private val watchProgressRepository: WatchProgressRepository,
    private val securePrefs: SecurePrefs
) : AndroidViewModel(application) {

    private val libVLC = LibVLC(
        application,
        arrayListOf(
            "--no-drop-late-frames",
            "--no-skip-frames",
            "--sub-text-scale=${securePrefs.subtitleScalePercent}"
        )
    )
    val mediaPlayer = MediaPlayer(libVLC)
    private var currentMedia: Media? = null

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

    private val _volumePercent = MutableStateFlow(100)
    val volumePercent: StateFlow<Int> = _volumePercent

    private val _audioTracks = MutableStateFlow<List<TrackOption>>(emptyList())
    val audioTracks: StateFlow<List<TrackOption>> = _audioTracks

    private val _currentAudioTrackId = MutableStateFlow(-1)
    val currentAudioTrackId: StateFlow<Int> = _currentAudioTrackId

    private val _subtitleTracks = MutableStateFlow<List<TrackOption>>(emptyList())
    val subtitleTracks: StateFlow<List<TrackOption>> = _subtitleTracks

    private val _currentSubtitleTrackId = MutableStateFlow(-1)
    val currentSubtitleTrackId: StateFlow<Int> = _currentSubtitleTrackId

    private val _subtitleScalePercent = MutableStateFlow(securePrefs.subtitleScalePercent)
    val subtitleScalePercent: StateFlow<Int> = _subtitleScalePercent

    private var pendingResumePositionMs: Long? = null
    private var progressSaveCounter = 0

    init {
        mediaPlayer.volume = _volumePercent.value

        mediaPlayer.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Playing -> {
                    _isPlaying.value = true
                    _isBuffering.value = false
                    _errorMessage.value = null
                    pendingResumePositionMs?.let { resumePos ->
                        mediaPlayer.time = resumePos
                        pendingResumePositionMs = null
                    }
                    refreshTracks()
                }
                MediaPlayer.Event.Paused -> _isPlaying.value = false
                MediaPlayer.Event.Buffering -> {
                    _isBuffering.value = event.buffering < 100f
                }
                MediaPlayer.Event.TimeChanged -> {
                    _currentPosition.value = event.timeChanged
                    val playerLength = mediaPlayer.length
                    if (playerLength > 0) {
                        _duration.value = playerLength
                    }
                    progressSaveCounter++
                    if (progressSaveCounter % 5 == 0) {
                        saveProgress()
                    }
                }
                MediaPlayer.Event.EncounteredError -> {
                    _isBuffering.value = false
                    _errorMessage.value = "Le lecteur de secours n'a pas réussi à lire ce contenu non plus."
                }
                MediaPlayer.Event.EndReached -> {
                    _isPlaying.value = false
                    saveProgress()
                }
            }
        }
    }

    private fun refreshTracks() {
        _audioTracks.value = mediaPlayer.audioTracks
            ?.map { TrackOption(it.id, it.name) }
            ?: emptyList()
        _currentAudioTrackId.value = mediaPlayer.audioTrack

        _subtitleTracks.value = mediaPlayer.spuTracks
            ?.map { TrackOption(it.id, it.name) }
            ?: emptyList()
        _currentSubtitleTrackId.value = mediaPlayer.spuTrack
    }

    fun selectAudioTrack(id: Int) {
        mediaPlayer.setAudioTrack(id)
        _currentAudioTrackId.value = id
    }

    fun selectSubtitleTrack(id: Int) {
        mediaPlayer.setSpuTrack(id)
        _currentSubtitleTrackId.value = id
    }

    fun setVolume(percent: Int) {
        val safe = percent.coerceIn(0, 150)
        mediaPlayer.volume = safe
        _volumePercent.value = safe
    }

    /**
     * S'applique à la prochaine lecture (libVLC ne permet pas de changer la
     * taille des sous-titres en direct sur une vidéo déjà en cours).
     */
    fun updateSubtitleScaleForNextPlayback(percent: Int) {
        val safe = percent.coerceIn(50, 200)
        _subtitleScalePercent.value = safe
        securePrefs.subtitleScalePercent = safe
    }

    fun seekRelative(deltaMs: Long) {
        val length = mediaPlayer.length.takeIf { it > 0 } ?: _duration.value
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
        _duration.value = 0L
        _currentChannel.value = channel
        progressSaveCounter = 0
        viewModelScope.launch {
            pendingResumePositionMs = watchProgressRepository.getSavedPosition(channel.id)
        }

        currentMedia?.release()

        val media = Media(libVLC, Uri.parse(channel.streamUrl))
        media.setHWDecoderEnabled(false, false)

        media.setEventListener { event ->
            if (event.type == IMedia.Event.ParsedChanged) {
                val parsedDuration = media.duration
                if (parsedDuration > 0 && _duration.value <= 0) {
                    _duration.value = parsedDuration
                }
            }
        }
        media.parseAsync(IMedia.Parse.ParseNetwork)

        currentMedia = media
        mediaPlayer.media = media
        mediaPlayer.play()
    }

    fun togglePlayPause() {
        if (mediaPlayer.isPlaying) mediaPlayer.pause() else mediaPlayer.play()
    }

    private fun saveProgress() {
        val channel = _currentChannel.value ?: return
        val position = mediaPlayer.time
        val length = mediaPlayer.length.takeIf { it > 0 } ?: _duration.value
        if (length <= 0) return
        viewModelScope.launch {
            watchProgressRepository.saveProgress(channel, position, length)
        }
    }

    override fun onCleared() {
        saveProgress()
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        mediaPlayer.release()
        currentMedia?.release()
        libVLC.release()
        super.onCleared()
    }
}
