package com.iptvplayer.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Récupère du contenu texte distant (M3U ou XMLTV) en HTTPS quand disponible.
 * Toute erreur réseau est convertie en exception claire pour être affichée à l'utilisateur.
 */
@Singleton
class HttpClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchText(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw java.io.IOException("Le serveur a répondu avec le code ${response.code}")
            }
            response.body?.string() ?: throw java.io.IOException("Réponse vide du serveur")
        }
    }
}
