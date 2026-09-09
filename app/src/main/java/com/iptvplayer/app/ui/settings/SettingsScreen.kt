package com.iptvplayer.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLoggedOut: () -> Unit
) {
    var epgUrl by remember { mutableStateOf(viewModel.epgUrl.value) }
    val syncing by viewModel.syncing.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Text(stringResource(R.string.settings_account), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Profil : ${viewModel.profileName.ifBlank { "Sans nom" }}")
        Text(
            "Playlist : ${viewModel.playlistUrl}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = epgUrl,
            onValueChange = {
                epgUrl = it
                viewModel.updateEpgUrl(it)
            },
            label = { Text(stringResource(R.string.settings_epg_url)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.syncEpgNow() },
            enabled = !syncing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (syncing) "Synchronisation…" else stringResource(R.string.settings_sync_epg_now))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.logout(onLoggedOut) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_logout))
        }
    }
}
