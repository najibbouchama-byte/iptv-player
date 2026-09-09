package com.iptvplayer.app.data.repository

import com.iptvplayer.app.data.local.SecurePrefs
import com.iptvplayer.app.data.model.EpgProgram
import com.iptvplayer.app.data.network.HttpClient
import com.iptvplayer.app.data.parser.XmlTvParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpgRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val securePrefs: SecurePrefs
) {
    // Programmes groupés par identifiant de chaîne EPG, pour un accès rapide
    private val _programsByChannel = MutableStateFlow<Map<String, List<EpgProgram>>>(emptyMap())
    val programsByChannel: StateFlow<Map<String, List<EpgProgram>>> = _programsByChannel

    /**
     * Télécharge et parse le fichier EPG XMLTV configuré par l'utilisateur.
     * Ne fait rien si aucune URL EPG n'est renseignée (fonctionnalité optionnelle).
     */
    suspend fun syncEpg() {
        val url = securePrefs.epgUrl?.takeIf { it.isNotBlank() } ?: return
        try {
            val rawXml = httpClient.fetchText(url)
            val programs = XmlTvParser.parse(rawXml)
            _programsByChannel.value = programs.groupBy { it.channelEpgId }
        } catch (e: Exception) {
            // L'EPG est optionnel : une erreur ici ne doit jamais bloquer le Live TV
        }
    }

    fun currentProgram(epgChannelId: String?): EpgProgram? {
        if (epgChannelId == null) return null
        val now = System.currentTimeMillis()
        return _programsByChannel.value[epgChannelId]?.firstOrNull { it.isCurrent(now) }
    }

    fun nextProgram(epgChannelId: String?): EpgProgram? {
        if (epgChannelId == null) return null
        val now = System.currentTimeMillis()
        return _programsByChannel.value[epgChannelId]
            ?.filter { it.startMillis > now }
            ?.minByOrNull { it.startMillis }
    }

    fun programsFor(epgChannelId: String?): List<EpgProgram> {
        if (epgChannelId == null) return emptyList()
        return _programsByChannel.value[epgChannelId].orEmpty()
    }
}
