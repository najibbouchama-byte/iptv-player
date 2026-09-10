package com.iptvplayer.app.data.network

import com.iptvplayer.app.data.model.Episode
import com.iptvplayer.app.data.model.Movie
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.XtreamCategory
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamApi @Inject constructor(
    private val httpClient: HttpClient
) {

    suspend fun getLiveCategories(creds: XtreamCredentials): List<XtreamCategory> {
        val json = httpClient.fetchJson("${creds.apiBaseUrl}&action=get_live_categories")
        return parseCategories(json)
    }

    suspend fun getVodCategories(creds: XtreamCredentials): List<XtreamCategory> {
        val json = httpClient.fetchJson("${creds.apiBaseUrl}&action=get_vod_categories")
        return parseCategories(json)
    }

    suspend fun getSeriesCategories(creds: XtreamCredentials): List<XtreamCategory> {
        val json = httpClient.fetchJson("${creds.apiBaseUrl}&action=get_series_categories")
        return parseCategories(json)
    }

    suspend fun getLiveStreams(creds: XtreamCredentials, categoryId: String): List<com.iptvplayer.app.data.model.Channel> {
        val json = httpClient.fetchJson("${creds.apiBaseUrl}&action=get_live_streams&category_id=$categoryId")
        val array = JSONArray(json)
        val result = mutableListOf<com.iptvplayer.app.data.model.Channel>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val streamId = obj.optString("stream_id")
            val name = obj.optString("name", "Chaîne sans nom")
            val icon = obj.optString("stream_icon").takeIf { it.isNotBlank() }
            val epgId = obj.optString("epg_channel_id").takeIf { it.isNotBlank() }
            val streamUrl = "${creds.baseUrl}/live/${creds.username}/${creds.password}/$streamId.ts"
            result += com.iptvplayer.app.data.model.Channel(
                id = streamId,
                name = name,
                logoUrl = icon,
                streamUrl = streamUrl,
                category = categoryId,
                epgChannelId = epgId
            )
        }
        return result
    }

    suspend fun getVodStreams(creds: XtreamCredentials, categoryId: String): List<Movie> {
        val json = httpClient.fetchJson("${creds.apiBaseUrl}&action=get_vod_streams&category_id=$categoryId")
        val array = JSONArray(json)
        val result = mutableListOf<Movie>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += Movie(
                id = obj.optString("stream_id"),
                name = obj.optString("name", "Film sans titre"),
                posterUrl = obj.optString("stream_icon").takeIf { it.isNotBlank() },
                categoryId = categoryId,
                containerExtension = obj.optString("container_extension", "mp4")
            )
        }
        return result
    }

    suspend fun getSeries(creds: XtreamCredentials, categoryId: String): List<Series> {
        val json = httpClient.fetchJson("${creds.apiBaseUrl}&action=get_series&category_id=$categoryId")
        val array = JSONArray(json)
        val result = mutableListOf<Series>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += Series(
                id = obj.optString("series_id"),
                name = obj.optString("name", "Série sans titre"),
                posterUrl = obj.optString("cover").takeIf { it.isNotBlank() },
                categoryId = categoryId,
                plot = obj.optString("plot").takeIf { it.isNotBlank() }
            )
        }
        return result
    }

    suspend fun getSeriesEpisodes(creds: XtreamCredentials, seriesId: String): List<Episode> {
        val json = httpClient.fetchJson("${creds.apiBaseUrl}&action=get_series_info&series_id=$seriesId")
        val root = JSONObject(json)
        val episodesBySeason = root.optJSONObject("episodes") ?: return emptyList()
        val result = mutableListOf<Episode>()
        val seasonKeys = episodesBySeason.keys()
        while (seasonKeys.hasNext()) {
            val seasonKey = seasonKeys.next()
            val seasonArray = episodesBySeason.optJSONArray(seasonKey) ?: continue
            for (i in 0 until seasonArray.length()) {
                val ep = seasonArray.getJSONObject(i)
                result += Episode(
                    id = ep.optString("id"),
                    title = ep.optString("title", "Épisode"),
                    seasonNumber = seasonKey.toIntOrNull() ?: 0,
                    episodeNumber = ep.optInt("episode_num", 0),
                    containerExtension = ep.optString("container_extension", "mp4")
                )
            }
        }
        return result.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
    }

    fun buildMovieStreamUrl(creds: XtreamCredentials, movie: Movie): String =
        "${creds.baseUrl}/movie/${creds.username}/${creds.password}/${movie.id}.${movie.containerExtension}"

    fun buildEpisodeStreamUrl(creds: XtreamCredentials, episode: Episode): String =
        "${creds.baseUrl}/series/${creds.username}/${creds.password}/${episode.id}.${episode.containerExtension}"

    private fun parseCategories(json: String): List<XtreamCategory> {
        val array = JSONArray(json)
        val result = mutableListOf<XtreamCategory>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += XtreamCategory(
                id = obj.optString("category_id"),
                name = obj.optString("category_name", "Sans nom")
            )
        }
        return result
    }
}
