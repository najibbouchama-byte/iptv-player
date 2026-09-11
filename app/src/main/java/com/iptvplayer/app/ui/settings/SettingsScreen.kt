package com.iptvplayer.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R

private enum class SettingsPage { MAIN, ACCOUNT, EPG, PLAYER, ABOUT }

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLoggedOut: () -> Unit
) {
    var page by remember { mutableStateOf(SettingsPage.MAIN) }

    BackHandler(enabled = page != SettingsPage.MAIN) {
        page = SettingsPage.MAIN
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page != SettingsPage.MAIN) {
                IconButton(onClick = { page = SettingsPage.MAIN }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = when (page) {
                    SettingsPage.MAIN -> stringResource(R.string.settings_title)
                    SettingsPage.ACCOUNT -> "Compte IPTV"
                    SettingsPage.EPG -> "Guide TV (EPG)"
                    SettingsPage.PLAYER -> "Lecteur vidéo"
                    SettingsPage.ABOUT -> stringResource(R.string.settings_about)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        when (page) {
            SettingsPage.MAIN -> MainSettingsPage(onNavigate = { page = it })
            SettingsPage.ACCOUNT -> AccountPage(viewModel = viewModel, onLoggedOut = onLoggedOut)
            SettingsPage.EPG -> EpgPage(viewModel = viewModel)
            SettingsPage.PLAYER -> PlayerPage(viewModel = viewModel)
            SettingsPage.ABOUT -> AboutPage()
        }
    }
}

@Composable
private fun MainSettingsPage(onNavigate: (SettingsPage) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        MenuRow(icon = Icons.Filled.Person, title = "Compte IPTV", onClick = { onNavigate(SettingsPage.ACCOUNT) })
        Spacer(modifier = Modifier.height(10.dp))
        MenuRow(icon = Icons.Filled.Tv, title = "Guide TV (EPG)", onClick = { onNavigate(SettingsPage.EPG) })
        Spacer(modifier = Modifier.height(10.dp))
        MenuRow(icon = Icons.Filled.Speed, title = "Lecteur vidéo", onClick = { onNavigate(SettingsPage.PLAYER) })
        Spacer(modifier = Modifier.height(10.dp))
        MenuRow(icon = Icons.Filled.Info, title = "À propos", onClick = { onNavigate(SettingsPage.ABOUT) })
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun AccountPage(viewModel: SettingsViewModel, onLoggedOut: () -> Unit) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        ProfileHeaderCard(
            profileName = viewModel.profileName.ifBlank { "Profil sans nom" },
            serverUrl = viewModel.playlistUrl.ifBlank { "Aucun serveur enregistré" }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { showLogoutConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.settings_logout))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Se déconnecter ?") },
            text = { Text("Tu devras ressaisir tes identifiants Xtream pour te reconnecter.") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; viewModel.logout(onLoggedOut) }) {
                    Text("Se déconnecter", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun EpgPage(viewModel: SettingsViewModel) {
    var epgUrl by remember { mutableStateOf(viewModel.epgUrl.value) }
    val syncing by viewModel.syncing.collectAsState()

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = epgUrl,
                    onValueChange = { epgUrl = it; viewModel.updateEpgUrl(it) },
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
                    if (syncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synchronisation…")
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synchroniser l'EPG", maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerPage(viewModel: SettingsViewModel) {
    val minBuffer by viewModel.minBufferSeconds.collectAsState()
    val maxBuffer by viewModel.maxBufferSeconds.collectAsState()

    Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Text("Durée du tampon", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "$minBuffer secondes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Durée que le lecteur précharge avant de démarrer la lecture. Une valeur plus élevée réduit les coupures, mais ajoute un délai au lancement et sur le direct.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Slider(
            value = minBuffer.toFloat(),
            onValueChange = { viewModel.updateMinBuffer(it.toInt()) },
            valueRange = 1f..15f,
            steps = 13
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Durée max du tampon", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "$maxBuffer secondes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Durée maximale que le lecteur peut précharger à l'avance. Des valeurs plus faibles évitent les problèmes de mémoire, des valeurs plus élevées réduisent les pauses de chargement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Slider(
            value = maxBuffer.toFloat(),
            onValueChange = { viewModel.updateMaxBuffer(it.toInt()) },
            valueRange = minBuffer.toFloat()..120f,
            steps = 23
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Le nouveau réglage s'applique à la prochaine lecture d'une chaîne.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AboutPage() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                InfoRow(icon = Icons.Filled.Info, title = "Application", value = "Infinity Player")
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                InfoRow(icon = Icons.Filled.CalendarMonth, title = "Version", value = "1.0")
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(profileName: String, serverUrl: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initial = profileName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initial, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Link,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
