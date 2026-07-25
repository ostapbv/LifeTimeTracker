package com.example.lifetimetracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lifetimetracker.domain.repository.UsageRepository
import com.example.lifetimetracker.domain.usecase.LimitEvaluationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@HiltWorker
class UsageSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageRepository: UsageRepository,
    private val limitEvaluationUseCase: LimitEvaluationUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!usageRepository.hasUsagePermission()) {
                return@withContext Result.success() // Can't do anything without permission
            }

            // Sync for today
            val today = LocalDate.now()
            usageRepository.syncUsageForDate(today)

            // Evaluate limits after sync
            limitEvaluationUseCase()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
