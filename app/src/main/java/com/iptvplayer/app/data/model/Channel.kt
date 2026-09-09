package com.iptvplayer.app.data.model

/**
 * Représente une chaîne TV extraite de la playlist M3U.
 */
data class Channel(
    val id: String,          // identifiant unique (tvg-id ou généré)
    val name: String,        // nom affiché de la chaîne
    val logoUrl: String?,    // URL du logo (tvg-logo), peut être nulle
    val streamUrl: String,   // URL du flux vidéo
    val category: String,    // catégorie / groupe (group-title)
    val epgChannelId: String? // identifiant utilisé pour retrouver le programme dans l'EPG
)
