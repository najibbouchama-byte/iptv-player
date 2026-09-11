package com.iptvplayer.app.data.repository

import com.iptvplayer.app.data.local.dao.WatchProgressDao
import com.iptvplayer.app.data.local.entity.WatchProgressEntity
import com.iptvplayer.app.data.model.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchProgressRepository @Inject constructor(
    private val dao: WatchProgressDao
) {
    fun observeContinueWatching(): Flow<List<WatchProgressEntity>> = dao.observeAll()

    suspend fun getSavedPosition(channelId: String): Long? {
        val entity = dao.getById(channelId) ?: return null
        if (entity.durationMs > 0 && entity.positionMs.toFloat() / entity.durationMs > 0.95f) return null
        return entity.positionMs.takeIf { it > 5000 }
    }

    suspend fun saveProgress(channel: Channel, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        if (positionMs.toFloat() / durationMs > 0.95f) {
            dao.deleteById(channel.id)
            return
        }
        dao.upsert(
            WatchProgressEntity(
                channelId = channel.id,
                name = channel.name,
                posterUrl = channel.logoUrl,
                streamUrl = channel.streamUrl,
                category = channel.category,
                positionMs = positionMs,
                durationMs = durationMs,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    fun toChannel(entity: WatchProgressEntity): Channel = Channel(
        id = entity.channelId,
        name = entity.name,
        logoUrl = entity.posterUrl,
        streamUrl = entity.streamUrl,
        category = entity.category,
        epgChannelId = null
    )
}
