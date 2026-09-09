package com.iptvplayer.app.data.parser

import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Playlist
import java.util.UUID

/**
 * Parseur de fichiers M3U / M3U8 au format IPTV classique :
 *
 * #EXTM3U
 * #EXTINF:-1 tvg-id="france2" tvg-logo="https://.../logo.png" group-title="Généralistes",France 2
 * http://serveur/flux1.m3u8
 *
 * Ce parseur est volontairement tolérant : si un attribut manque, on utilise une valeur par défaut
 * plutôt que de faire planter l'import (les playlists IPTV réelles sont souvent mal formées).
 */
object M3uParser {

    private val attributeRegex = Regex("""([a-zA-Z0-9_-]+)="([^"]*)"""")

    fun parse(rawContent: String): Playlist {
        val lines = rawContent.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val channels = mutableListOf<Channel>()
        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingCategory: String = "Autres"
        var pendingTvgId: String? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTM3U") -> {
                    // en-tête du fichier, rien à faire
                }
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val attributes = attributeRegex.findAll(line)
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }

                    pendingLogo = attributes["tvg-logo"]
                    pendingCategory = attributes["group-title"]?.takeIf { it.isNotBlank() } ?: "Autres"
                    pendingTvgId = attributes["tvg-id"]?.takeIf { it.isNotBlank() }

                    // Le nom de la chaîne est le texte après la dernière virgule de la ligne EXTINF
                    pendingName = line.substringAfterLast(",").trim().ifBlank { "Chaîne sans nom" }
                }
                line.startsWith("#") -> {
                    // autre métadonnée (#EXTGRP, #EXTVLCOPT, etc.) : ignorée pour l'instant
                }
                else -> {
                    // Ligne sans "#" = URL du flux, ferme l'entrée en cours
                    val name = pendingName ?: "Chaîne sans nom"
                    channels += Channel(
                        id = pendingTvgId ?: UUID.nameUUIDFromBytes(line.toByteArray()).toString(),
                        name = name,
                        logoUrl = pendingLogo,
                        streamUrl = line,
                        category = pendingCategory,
                        epgChannelId = pendingTvgId
                    )
                    pendingName = null
                    pendingLogo = null
                    pendingCategory = "Autres"
                    pendingTvgId = null
                }
            }
        }

        return Playlist(channels)
    }
}
