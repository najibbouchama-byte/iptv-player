package com.iptvplayer.app.data.repository

import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Episode
import com.iptvplayer.app.data.model.Movie
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.XtreamCategory
import com.iptvplayer.app.data.network.XtreamApi
import com.iptvplayer.app.data.network.XtreamCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamRepository @Inject constructor(
    private val xtreamApi: XtreamApi,
    private val securePrefs: SecurePrefs
) {
    private var credentials: XtreamCredentials? = null

    private val _liveCategories = MutableStateFlow<List<XtreamCategory>>(emptyList())
    val liveCategories: StateFlow<List<XtreamCategory>> = _liveCategories

    private val _vodCategories = MutableStateFlow<List<XtreamCategory>>(emptyList())
    val vodCategories: StateFlow<List<XtreamCategory>> = _vodCategories

    private val _seriesCategories = MutableStateFlow<List<XtreamCategory>>(emptyList())
    val seriesCategories: StateFlow<List<XtreamCategory>> = _seriesCategories

    private val liveStreamsCache = mutableMapOf<String, List<Channel>>()
    private val vodStreamsCache = mutableMapOf<String, List<Movie>>()
    private val seriesCache = mutableMapOf<String, List<Series>>()

    val isConnected: Boolean get() = credentials != null

    suspend fun connect(m3uUrl: String): Boolean {
        val creds = XtreamCredentials.parse(m3uUrl) ?: return false
        val categories = xtreamApi.getLiveCategories(creds)
        credentials = creds
        _liveCategories.value = categories
        _vodCategories.value = emptyList()
        _seriesCategories.value = emptyList()
        liveStreamsCache.clear()
        vodStreamsCache.clear()
        seriesCache.clear()
        return true
    }

    suspend fun reconnectFromSavedUrl(): Boolean {
        val url = securePrefs.playlistUrl ?: return false
        return connect(url)
    }

    suspend fun loadVodCategoriesIfNeeded() {
        if (_vodCategories.value.isNotEmpty()) return
        val creds = credentials ?: return
        _vodCategories.value = xtreamApi.getVodCategories(creds)
    }

    suspend fun loadSeriesCategoriesIfNeeded() {
        if (_seriesCategories.value.isNotEmpty()) return
        val creds = credentials ?: return
        _seriesCategories.value = xtreamApi.getSeriesCategories(creds)
    }

    suspend fun getLiveStreams(categoryId: String): List<Channel> {
        liveStreamsCache[categoryId]?.let { return it }
        val creds = credentials ?: return emptyList()
        val streams = xtreamApi.getLiveStreams(creds, categoryId)
        liveStreamsCache[categoryId] = streams
        return streams
    }

    suspend fun getVodStreams(categoryId: String): List<Movie> {
        vodStreamsCache[categoryId]?.let { return it }
        val creds = credentials ?: return emptyList()
        val streams = xtreamApi.getVodStreams(creds, categoryId)
        vodStreamsCache[categoryId] = streams
        return streams
    }

    suspend fun getSeries(categoryId: String): List<Series> {
        seriesCache[categoryId]?.let { return it }
        val creds = credentials ?: return emptyList()
        val list = xtreamApi.getSeries(creds, categoryId)
        seriesCache[categoryId] = list
        return list
    }

    suspend fun getSeriesEpisodes(seriesId: String): List<Episode> {
        val creds = credentials ?: return emptyList()
        return xtreamApi.getSeriesEpisodes(creds, seriesId)
    }

    fun movieStreamUrl(movie: Movie): String? =
        credentials?.let { xtreamApi.buildMovieStreamUrl(it, movie) }

    fun episodeStreamUrl(episode: Episode): String? =
        credentials?.let { xtreamApi.buildEpisodeStreamUrl(it, episode) }
}
