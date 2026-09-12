package com.iptvplayer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un film ou une série ajouté par l'utilisateur à "Ma liste" depuis l'Accueil.
 */
@Entity(tableName = "my_list")
data class MyListEntity(
    @PrimaryKey val itemId: String,
    val type: String, // "movie" ou "series"
    val name: String,
    val posterUrl: String?,
    val streamUrl: String?,
    val addedAtMillis: Long
)
