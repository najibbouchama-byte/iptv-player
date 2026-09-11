package com.iptvplayer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_progress")
data class WatchProgressEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val posterUrl: String?,
    val streamUrl: String,
    val category: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAtMillis: Long
)
