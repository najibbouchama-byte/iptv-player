package com.iptvplayer.app.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.iptvplayer.app.data.model.Channel

@Composable
fun PlayerScreen(
    channel: Channel,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(channel.id) {
        viewModel.playChannel(channel)
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
            viewModel.exoPlayer.pause()
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = viewModel.exoPlayer
                    useController = false
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering && errorMessage == null) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentChannel?.name ?: channel.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                viewModel.currentProgramTitle()?.let {
                    Text(text = it, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = { toggleOrientation(context as? Activity) }) {
                Icon(Icons.Filled.ScreenRotation, contentDescription = "Rotation", tint = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.switchChannel(-1) }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Chaîne précédente", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(24.dp))
            IconButton(onClick = { viewModel.togglePlayPause() }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Lecture / Pause",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            IconButton(onClick = { viewModel.switchChannel(1) }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Chaîne suivante", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
    }
}

private fun toggleOrientation(activity: Activity?) {
    activity ?: return
    activity.requestedOrientation = if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } else {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}
