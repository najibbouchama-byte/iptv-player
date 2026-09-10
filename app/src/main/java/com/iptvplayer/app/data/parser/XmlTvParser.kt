package com.iptvplayer.app.data.parser

import com.iptvplayer.app.data.model.EpgProgram
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.Reader
import java.text.SimpleDateFormat
import java.util.Locale

object XmlTvParser {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

    fun parse(reader: Reader): List<EpgProgram> {
        val programs = mutableListOf<EpgProgram>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(reader)

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
