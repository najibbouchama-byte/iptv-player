package com.iptvplayer.app.data.network

import android.net.Uri

data class XtreamCredentials(
    val baseUrl: String,
    val username: String,
    val password: String
) {
    val apiBaseUrl: String
        get() = "$baseUrl/player_api.php?username=$username&password=$password"

    companion object {
        fun parse(m3uUrl: String): XtreamCredentials? {
            return try {
                val uri = Uri.parse(m3uUrl)
                val username = uri.getQueryParameter("username") ?: return null
                val password = uri.getQueryParameter("password") ?: return null
                val scheme = uri.scheme ?: "http"
                val host = uri.host ?: return null
                val port = uri.port
                val baseUrl = if (port != -1) "$scheme://$host:$port" else "$scheme://$host"
                XtreamCredentials(baseUrl, username, password)
            } catch (e: Exception) {
                null
            }
        }
    }
}
