package com.iptvplayer.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stocke les informations sensibles (identifiants Xtream, URL de l'EPG, nom
 * du profil) dans un fichier CHIFFRÉ sur le téléphone (AES-256), jamais en
 * clair et jamais dans le code source.
 */
@Singleton
class SecurePrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "iptv_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /** URL du serveur Xtream (ex: http://serveur.exemple:8080), sans player_api.php */
    var xtreamServerUrl: String?
        get() = prefs.getString(KEY_XTREAM_SERVER_URL, null)
        set(value) = prefs.edit().putString(KEY_XTREAM_SERVER_URL, value).apply()

    var xtreamUsername: String?
        get() = prefs.getString(KEY_XTREAM_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_XTREAM_USERNAME, value).apply()

    var xtreamPassword: String?
        get() = prefs.getString(KEY_XTREAM_PASSWORD, null)
        set(value) = prefs.edit().putString(KEY_XTREAM_PASSWORD, value).apply()

    /** MODE_XTREAM ou MODE_M3U : indique comment la session a été créée */
    var connectionMode: String?
        get() = prefs.getString(KEY_CONNECTION_MODE, MODE_XTREAM)
        set(value) = prefs.edit().putString(KEY_CONNECTION_MODE, value).apply()

    /** Conservé pour compatibilité avec les sessions créées avant la migration, et pour l'affichage dans Paramètres */
    var playlistUrl: String?
        get() = prefs.getString(KEY_PLAYLIST_URL, null)
        set(value) = prefs.edit().putString(KEY_PLAYLIST_URL, value).apply()

    var epgUrl: String?
        get() = prefs.getString(KEY_EPG_URL, null)
        set(value) = prefs.edit().putString(KEY_EPG_URL, value).apply()

    var profileName: String?
        get() = prefs.getString(KEY_PROFILE_NAME, null)
        set(value) = prefs.edit().putString(KEY_PROFILE_NAME, value).apply()

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()

    /** Durée en secondes que le lecteur précharge avant de démarrer la lecture */
    var minBufferSeconds: Int
        get() = prefs.getInt(KEY_MIN_BUFFER, 5)
        set(value) = prefs.edit().putInt(KEY_MIN_BUFFER, value).apply()

    /** Durée maximale en secondes que le lecteur peut précharger à l'avance */
    var maxBufferSeconds: Int
        get() = prefs.getInt(KEY_MAX_BUFFER, 50)
        set(value) = prefs.edit().putInt(KEY_MAX_BUFFER, value).apply()

    /** Taille des sous-titres du lecteur VLC (Films/Séries), en pourcentage. 100 = taille normale */
    var subtitleScalePercent: Int
        get() = prefs.getInt(KEY_SUBTITLE_SCALE, 100)
        set(value) = prefs.edit().putInt(KEY_SUBTITLE_SCALE, value).apply()

    fun isLoggedIn(): Boolean {
        if (!rememberMe) return false
        val hasXtreamCredentials =
            !xtreamServerUrl.isNullOrBlank() && !xtreamUsername.isNullOrBlank() && !xtreamPassword.isNullOrBlank()
        val hasLegacyUrl = !playlistUrl.isNullOrBlank()
        return hasXtreamCredentials || hasLegacyUrl
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_PLAYLIST_URL = "playlist_url"
        private const val KEY_EPG_URL = "epg_url"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_XTREAM_SERVER_URL = "xtream_server_url"
        private const val KEY_XTREAM_USERNAME = "xtream_username"
        private const val KEY_XTREAM_PASSWORD = "xtream_password"
        private const val KEY_CONNECTION_MODE = "connection_mode"
        private const val KEY_MIN_BUFFER = "min_buffer_seconds"
        private const val KEY_MAX_BUFFER = "max_buffer_seconds"
        private const val KEY_SUBTITLE_SCALE = "subtitle_scale_percent"

        const val MODE_XTREAM = "XTREAM"
        const val MODE_M3U = "M3U"
    }
}
