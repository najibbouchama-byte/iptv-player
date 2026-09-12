package com.iptvplayer.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iptvplayer.app.data.local.dao.FavoriteDao
import com.iptvplayer.app.data.local.dao.HistoryDao
import com.iptvplayer.app.data.local.dao.MyListDao
import com.iptvplayer.app.data.local.dao.WatchProgressDao
import com.iptvplayer.app.data.local.entity.FavoriteEntity
import com.iptvplayer.app.data.local.entity.HistoryEntity
import com.iptvplayer.app.data.local.entity.MyListEntity
import com.iptvplayer.app.data.local.entity.WatchProgressEntity

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class, WatchProgressEntity::class, MyListEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun watchProgressDao(): WatchProgressDao
    abstract fun myListDao(): MyListDao
}
