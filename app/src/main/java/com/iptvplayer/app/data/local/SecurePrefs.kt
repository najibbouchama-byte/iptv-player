package com.iptvplayer.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stocke les informations sensibles (URL de la playlist M3U, URL de l'EPG, nom du profil)
 * dans un fichier CHIFFRÉ sur le téléphone (AES-256), jamais en clair et jamais dans le code source.
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

    fun isLoggedIn(): Boolean = rememberMe && !playlistUrl.isNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_PLAYLIST_URL = "playlist_url"
        private const val KEY_EPG_URL = "epg_url"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_REMEMBER_ME = "remember_me"
    }
}
