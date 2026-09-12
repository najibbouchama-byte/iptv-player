package com.iptvplayer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iptvplayer.app.data.local.entity.MyListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyListDao {
    @Query("SELECT * FROM my_list ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<MyListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MyListEntity)

    @Query("DELETE FROM my_list WHERE itemId = :itemId")
    suspend fun deleteById(itemId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM my_list WHERE itemId = :itemId)")
    suspend fun isInList(itemId: String): Boolean
}
