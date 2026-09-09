package com.iptvplayer.app.data.model

/**
 * Résultat complet du parsing d'une playlist M3U : liste des chaînes et catégories déduites.
 */
data class Playlist(
    val channels: List<Channel>
) {
    val categories: List<String>
        get() = channels.map { it.category }.distinct().sorted()
}
