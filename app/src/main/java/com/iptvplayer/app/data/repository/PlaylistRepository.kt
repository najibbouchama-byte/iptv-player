package com.iptvplayer.app.data.repository

import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.local.dao.FavoriteDao
import com.iptvplayer.app.data.local.dao.HistoryDao
import com.iptvplayer.app.data.local.entity.FavoriteEntity
import com.iptvplayer.app.data.local.entity.HistoryEntity
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Playlist
import com.iptvplayer.app.data.network.HttpClient
import com.iptvplayer.app.data.parser.M3uParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val securePrefs: SecurePrefs,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao
) {
    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist: StateFlow<Playlist?> = _playlist

    suspend fun loadPlaylist(url: String): Playlist {
        val parsed = httpClient.fetchAndParse(url) { reader -> M3uParser.parse(reader) }
        if (parsed.channels.isEmpty()) {
            throw IllegalStateException("La playlist ne contient aucune chaîne valide")
        }
        _playlist.value = parsed
        return parsed
    }

    suspend fun reloadSavedPlaylist(): Playlist? {
        val url = securePrefs.playlistUrl ?: return null
        return loadPlaylist(url)
    }

    fun observeFavorites(): Flow<List<Channel>> =
        favoriteDao.observeAll().map { list ->
            list.map {
                Channel(it.channelId, it.name, it.logoUrl, it.streamUrl, it.category, it.epgChannelId)
            }
        }

    fun observeRecent(): Flow<List<Channel>> =
        historyDao.observeRecent().map { list ->
            list.map {
                Channel(it.channelId, it.name, it.logoUrl, it.streamUrl, it.category, it.epgChannelId)
            }
        }

    suspend fun toggleFavorite(channel: Channel) {
        if (favoriteDao.isFavorite(channel.id)) {
            favoriteDao.deleteById(channel.id)
        } else {
            favoriteDao.insert(
                FavoriteEntity(
                    channelId = channel.id,
                    name = channel.name,
                    logoUrl = channel.logoUrl,
                    streamUrl = channel.streamUrl,
                    category = channel.category,
                    epgChannelId = channel.epgChannelId,
                    addedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun isFavorite(channelId: String): Boolean = favoriteDao.isFavorite(channelId)

    suspend fun recordWatched(channel: Channel) {
        historyDao.insert(
            HistoryEntity(
                channelId = channel.id,
                name = channel.name,
                logoUrl = channel.logoUrl,
                streamUrl = channel.streamUrl,
                category = channel.category,
                epgChannelId = channel.epgChannelId,
                watchedAtMillis = System.currentTimeMillis()
            )
        )
    }
}
