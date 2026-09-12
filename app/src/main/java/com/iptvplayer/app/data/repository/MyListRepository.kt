package com.iptvplayer.app.data.repository

import com.iptvplayer.app.data.local.dao.MyListDao
import com.iptvplayer.app.data.local.entity.MyListEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyListRepository @Inject constructor(
    private val dao: MyListDao
) {
    fun observeAll(): Flow<List<MyListEntity>> = dao.observeAll()

    fun observeByType(type: String): Flow<List<MyListEntity>> = dao.observeByType(type)

    suspend fun remove(itemId: String) {
        dao.deleteById(itemId)
    }

    suspend fun toggle(
        itemId: String,
        type: String,
        name: String,
        posterUrl: String?,
        streamUrl: String?,
        categoryId: String? = null
    ) {
        if (dao.isInList(itemId)) {
            dao.deleteById(itemId)
        } else {
            dao.insert(
                MyListEntity(
                    itemId = itemId,
                    type = type,
                    name = name,
                    posterUrl = posterUrl,
                    streamUrl = streamUrl,
                    categoryId = categoryId,
                    addedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }
}
