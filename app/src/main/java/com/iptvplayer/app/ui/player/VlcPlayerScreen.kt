package com.iptvplayer.app.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.data.model.Channel
import kotlinx.coroutines.delay
import org.videolan.libvlc.util.VLCVideoLayout
import java.util.Locale

@Composable
fun VlcPlayerScreen(
    channel: Channel,
    viewModel: VlcPlayerViewModel = hiltViewModel(),
    hasNext: Boolean = false,
    hasPrevious: Boolean = false,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val aspectLabel by viewModel.aspectLabel.collectAsState()
    val volumePercent by viewModel.volumePercent.collectAsState()
    val audioTracks by viewModel.audioTracks.collectAsState()
    val currentAudioTrackId by viewModel.currentAudioTrackId.collectAsState()
    val subtitleTracks by viewModel.subtitleTracks.collectAsState()
    val currentSubtitleTrackId by viewModel.currentSubtitleTrackId.collectAsState()
    val subtitleScalePercent by viewModel.subtitleScalePercent.collectAsState()

    var draggingPosition by remember { mutableStateOf<Float?>(null) }
    var showTracksPanel by remember { mutableStateOf(false) }

    var controlsVisible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableStateOf(0) }

    fun keepControlsVisible() {
        controlsVisible = true
        interactionTick++
    }

    LaunchedEffect(interactionTick) {
        if (controlsVisible) {
            delay(5000)
            controlsVisible = false
        }
    }

    LaunchedEffect(channel.id) {
        viewModel.play(channel)
        keepControlsVisible()
    }

    BackHandler(onBack = onBack)

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, view)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            viewModel.mediaPlayer.stop()
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if (showTracksPanel) {
                        showTracksPanel = false
                    } else if (controlsVisible) {
                        controlsVisible = false
                    } else {
                        keepControlsVisible()
                    }
                })
            }
    ) {
        AndroidView(
            factory = { ctx ->
                VLCVideoLayout(ctx).also { layout ->
                    viewModel.mediaPlayer.attachViews(layout, null, false, false)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering && errorMessage == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Impossible de lire ce flux",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (controlsVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }
                Text(
                    text = currentChannel?.name ?: channel.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showTracksPanel = true; keepControlsVisible() }) {
                    Icon(Icons.Filled.ClosedCaption, contentDescription = "Audio et sous-titres", tint = Color.White)
                }
                IconButton(onClick = { viewModel.cycleAspectRatio(); keepControlsVisible() }) {
                    Icon(Icons.Filled.AspectRatio, contentDescription = "Ajuster l'image ($aspectLabel)", tint = Color.White)
                }
            }

            // Volume vertical sur le bord droit
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .height(220.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color.White)
                VerticalVolumeBar(
                    volumePercent = volumePercent,
                    onVolumeChange = {
                        viewModel.setVolume(it)
                        keepControlsVisible()
                    },
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (duration > 0) {
                    val sliderValue = draggingPosition ?: currentPosition.toFloat()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatMillis(sliderValue.toLong()),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                draggingPosition = it
                                keepControlsVisible()
                            },
                            onValueChangeFinished = {
                                draggingPosition?.let { viewModel.seekTo(it.toLong()) }
                                draggingPosition = null
                            },
                            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text(
                            text = formatMillis(duration),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onPrevious(); keepControlsVisible() },
                        enabled = hasPrevious
                    ) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "Épisode précédent",
                            tint = if (hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.seekRelative(-10_000); keepControlsVisible() }) {
                        Icon(Icons.Filled.Replay10, contentDescription = "Reculer de 10 secondes", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { viewModel.togglePlayPause(); keepControlsVisible() }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Lecture / Pause",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { viewModel.seekRelative(10_000); keepControlsVisible() }) {
                        Icon(Icons.Filled.Forward10, contentDescription = "Avancer de 10 secondes", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    IconButton(
                        onClick = { onNext(); keepControlsVisible() },
                        enabled = hasNext
                    ) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "Épisode suivant",
                            tint = if (hasNext) Color.White else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        if (showTracksPanel) {
            TracksPanel(
                audioTracks = audioTracks,
                currentAudioTrackId = currentAudioTrackId,
                subtitleTracks = subtitleTracks,
                currentSubtitleTrackId = currentSubtitleTrackId,
                subtitleScalePercent = subtitleScalePercent,
                onSelectAudio = { viewModel.selectAudioTrack(it); keepControlsVisible() },
                onSelectSubtitle = { viewModel.selectSubtitleTrack(it); keepControlsVisible() },
                onSubtitleScaleChange = { viewModel.updateSubtitleScaleForNextPlayback(it) },
                onDismiss = { showTracksPanel = false },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun VerticalVolumeBar(
    volumePercent: Int,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVolume = 150f
    Box(
        modifier = modifier
            .width(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, _ ->
                    val heightPx = size.height.toFloat().coerceAtLeast(1f)
                    val relative = 1f - (change.position.y / heightPx).coerceIn(0f, 1f)
                    onVolumeChange((relative * maxVolume).toInt())
                }
            }
    ) {
        val fraction = (volumePercent / maxVolume).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(fraction)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun TracksPanel(
    audioTracks: List<TrackOption>,
    currentAudioTrackId: Int,
    subtitleTracks: List<TrackOption>,
    currentSubtitleTrackId: Int,
    subtitleScalePercent: Int,
    onSelectAudio: (Int) -> Unit,
    onSelectSubtitle: (Int) -> Unit,
    onSubtitleScaleChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF11142A))
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures { } } // absorbe les taps pour ne pas fermer le panneau
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TrackColumn(
                title = "Audio (${audioTracks.size} piste${if (audioTracks.size > 1) "s" else ""})",
                tracks = audioTracks,
                currentId = currentAudioTrackId,
                onSelect = onSelectAudio,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            TrackColumn(
                title = "Sous-titres (${subtitleTracks.size} piste${if (subtitleTracks.size > 1) "s" else ""})",
                tracks = subtitleTracks,
                currentId = currentSubtitleTrackId,
                onSelect = onSelectSubtitle,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Taille des sous-titres",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "S'applique à la prochaine lecture",
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall
        )
        Slider(
            value = subtitleScalePercent.toFloat(),
            onValueChange = { onSubtitleScaleChange(it.toInt()) },
            valueRange = 50f..200f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            )
        )
    }
}

@Composable
private fun TrackColumn(
    title: String,
    tracks: List<TrackOption>,
    currentId: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (tracks.isEmpty()) {
            Text(
                "Aucune",
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                items(tracks) { track ->
                    val isSelected = track.id == currentId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(track.id) }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            text = track.name,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
