package com.iptvplayer.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iptvplayer.app.data.repository.EpgRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Tâche de fond exécutée une fois par jour pour rafraîchir le guide TV (EPG),
 * comme demandé : synchronisation en arrière-plan plutôt qu'à chaque démarrage.
 */
@HiltWorker
class EpgSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val epgRepository: EpgRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            epgRepository.syncEpg()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "epg_daily_sync"
    }
}
