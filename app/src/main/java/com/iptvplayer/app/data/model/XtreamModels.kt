package com.iptvplayer.app.data.model

data class XtreamCategory(
    val id: String,
    val name: String
)

data class Movie(
    val id: String,
    val name: String,
    val posterUrl: String?,
    val categoryId: String,
    val containerExtension: String
)

data class Series(
    val id: String,
    val name: String,
    val posterUrl: String?,
    val categoryId: String,
    val plot: String? = null
)

data class Episode(
    val id: String,
    val title: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val containerExtension: String
)

/**
 * Résultat de l'authentification native auprès de l'API Xtream Codes
 * (appel à player_api.php sans action, qui renvoie le bloc "user_info").
 */
sealed interface XtreamAuthResult {
    data class Success(
        val expiresAt: String? = null,
        val maxConnections: Int? = null,
        val activeConnections: Int? = null
    ) : XtreamAuthResult

    data class Failure(val reason: String) : XtreamAuthResult
}
