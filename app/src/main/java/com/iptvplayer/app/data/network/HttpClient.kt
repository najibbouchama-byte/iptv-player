package com.iptvplayer.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(180, TimeUnit.SECONDS)
        .build()

    suspend fun <T> fetchAndParse(url: String, parse: (BufferedReader) -> T): T = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; IPTV Player) AppleWebKit/537.36")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw java.io.IOException("Le serveur a répondu avec le code ${response.code}")
            }
            val body = response.body ?: throw java.io.IOException("Réponse vide du serveur")
            body.charStream().buffered().use { reader ->
                parse(reader)
            }
        }
    }

    suspend fun fetchJson(url: String): String = fetchAndParse(url) { reader -> reader.readText() }
}
