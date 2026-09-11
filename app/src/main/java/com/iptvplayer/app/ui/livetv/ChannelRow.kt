package com.iptvplayer.app.ui.livetv

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.ChannelLogos
import com.iptvplayer.app.data.model.EpgProgram
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChannelRow(
    channel: Channel,
    isFavorite: Boolean,
    currentProgram: EpgProgram?,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    showLogo: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(onClick = onClick, onLongClick = onToggleFavorite)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChannelLogo(channel = channel, showLogo = showLogo)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (currentProgram != null) {
                Text(
                    text = currentProgram.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = channel.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favori",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Logo de chaîne avec bascule automatique en cas d'échec :
 * 1. essaie le stream_icon du panel Xtream
 * 2. si ça échoue (lien mort), essaie le logo de secours (repo tv-logos)
 * 3. si ça échoue aussi, affiche un badge à initiales sur fond coloré
 */
@Composable
private fun ChannelLogo(channel: Channel, showLogo: Boolean) {
    val candidates = remember(channel.id, channel.logoUrl) {
        ChannelLogos.candidateUrls(channel.name, channel.logoUrl)
    }
    var attemptIndex by remember(channel.id, channel.logoUrl) { mutableStateOf(0) }
    val currentUrl = candidates.getOrNull(attemptIndex)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (currentUrl != null && showLogo) {
            AsyncImage(
                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(currentUrl)
                    .size(96)
                    .scale(Scale.FIT)
                    .crossfade(false)
                    .build(),
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize(),
                onError = { attemptIndex += 1 }
            )
        } else {
            InitialsBadge(name = channel.name)
        }
    }
}

@Composable
private fun InitialsBadge(name: String) {
    val cleaned = name
        .replace(Regex("[|#]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    val words = cleaned.split(" ").filter { it.isNotBlank() }
    val initials = when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}"
        words.size == 1 && words[0].length >= 2 -> words[0].take(2)
        words.size == 1 -> words[0]
        else -> "?"
    }.uppercase()

    val palette = listOf(
        Color(0xFFE63946), Color(0xFF3A86FF), Color(0xFF8338EC),
        Color(0xFFFB5607), Color(0xFF2A9D8F), Color(0xFFE07A5F),
        Color(0xFF06D6A0), Color(0xFFD62828)
    )
    val color = palette[(cleaned.hashCode() and 0x7FFFFFFF) % palette.size]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
