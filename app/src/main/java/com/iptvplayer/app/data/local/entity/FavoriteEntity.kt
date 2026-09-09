package com.iptvplayer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val logoUrl: String?,
    val streamUrl: String,
    val category: String,
    val epgChannelId: String?,
    val addedAtMillis: Long
)
