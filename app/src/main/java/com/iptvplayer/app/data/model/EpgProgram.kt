package com.iptvplayer.app.data.model

/**
 * Représente un programme TV issu du fichier EPG (XMLTV).
 * startMillis / stopMillis sont des timestamps epoch en millisecondes.
 */
data class EpgProgram(
    val channelEpgId: String,
    val title: String,
    val description: String?,
    val startMillis: Long,
    val stopMillis: Long
) {
    fun isCurrent(nowMillis: Long): Boolean = nowMillis in startMillis until stopMillis
}
