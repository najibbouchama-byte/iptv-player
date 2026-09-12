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

    suspend fun toggle(
        itemId: String,
        type: String,
        name: String,
        posterUrl: String?,
        streamUrl: String?
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
                    addedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }
}
