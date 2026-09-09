package com.iptvplayer.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iptvplayer.app.data.local.dao.FavoriteDao
import com.iptvplayer.app.data.local.dao.HistoryDao
import com.iptvplayer.app.data.local.entity.FavoriteEntity
import com.iptvplayer.app.data.local.entity.HistoryEntity

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
}
