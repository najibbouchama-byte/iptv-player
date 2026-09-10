package com.iptvplayer.app.data.parser

import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Playlist
import java.io.BufferedReader
import java.util.UUID

object M3uParser {

    private val attributeRegex = Regex("""([a-zA-Z0-9_-]+)="([^"]*)"""")

    fun parse(reader: BufferedReader): Playlist {
        val channels = mutableListOf<Channel>()
        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingCategory: String = "Autres"
        var pendingTvgId: String? = null

        reader.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            when {
                line.startsWith("#EXTM3U") -> {
                }
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val attributes = attributeRegex.findAll(line)
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }

                    pendingLogo = attributes["tvg-logo"]
                    pendingCategory = attributes["group-title"]?.takeIf { it.isNotBlank() } ?: "Autres"
                    pendingTvgId = attributes["tvg-id"]?.takeIf { it.isNotBlank() }
                    pendingName = line.substringAfterLast(",").trim().ifBlank { "Chaîne sans nom" }
                }
                line.startsWith("#") -> {
                }
                else -> {
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
