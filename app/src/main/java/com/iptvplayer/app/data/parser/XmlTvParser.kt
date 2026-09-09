package com.iptvplayer.app.data.parser

import com.iptvplayer.app.data.model.EpgProgram
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Parseur XMLTV (format standard des guides EPG IPTV) :
 *
 * <programme start="20260909200000 +0200" stop="20260909220000 +0200" channel="france2">
 *   <title>Le journal</title>
 *   <desc>Résumé...</desc>
 * </programme>
 *
 * Si l'EPG n'est pas fourni ou mal formé, on retourne une liste vide sans planter l'appli :
 * l'EPG est une fonctionnalité optionnelle ("lorsque les données sont disponibles").
 */
object XmlTvParser {

    // Format XMLTV : yyyyMMddHHmmss Z (le "Z" ici représente le fuseau, ex: +0200)
    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

    fun parse(rawXml: String): List<EpgProgram> {
        val programs = mutableListOf<EpgProgram>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(rawXml))

            var eventType = parser.eventType
            var currentChannel: String? = null
            var currentStart: Long? = null
            var currentStop: Long? = null
            var currentTitle: String? = null
            var currentDesc: String? = null
            var textBuffer = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        textBuffer = StringBuilder()
                        if (parser.name == "programme") {
                            currentChannel = parser.getAttributeValue(null, "channel")
                            currentStart = safeParseDate(parser.getAttributeValue(null, "start"))
                            currentStop = safeParseDate(parser.getAttributeValue(null, "stop"))
                            currentTitle = null
                            currentDesc = null
                        }
                    }
                    XmlPullParser.TEXT -> {
                        textBuffer.append(parser.text)
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "title" -> currentTitle = textBuffer.toString().trim()
                            "desc" -> currentDesc = textBuffer.toString().trim()
                            "programme" -> {
                                val channel = currentChannel
                                val start = currentStart
                                val stop = currentStop
                                val title = currentTitle
                                if (channel != null && start != null && stop != null && title != null) {
                                    programs += EpgProgram(
                                        channelEpgId = channel,
                                        title = title,
                                        description = currentDesc,
                                        startMillis = start,
                                        stopMillis = stop
                                    )
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // On avale l'erreur : un EPG mal formé ne doit jamais faire planter l'application
            return programs
        }
        return programs
    }

    private fun safeParseDate(raw: String?): Long? {
        if (raw == null) return null
        return try {
            dateFormat.parse(raw)?.time
        } catch (e: Exception) {
            null
        }
    }
}
