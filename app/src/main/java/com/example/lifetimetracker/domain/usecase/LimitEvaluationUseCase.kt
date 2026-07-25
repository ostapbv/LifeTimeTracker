package com.example.lifetimetracker.domain.usecase

import com.example.lifetimetracker.data.local.dao.ActivityLogDao
import com.example.lifetimetracker.data.local.dao.CategoryDao
import com.example.lifetimetracker.data.local.dao.NotificationStateDao
import com.example.lifetimetracker.data.local.dao.UsageAggregateDao
import com.example.lifetimetracker.data.local.entity.NotificationStateEntity
import com.example.lifetimetracker.data.system.NotificationHelper
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class LimitEvaluationUseCase @Inject constructor(
    private val categoryDao: CategoryDao,
    private val activityLogDao: ActivityLogDao,
    private val usageAggregateDao: UsageAggregateDao,
    private val notificationStateDao: NotificationStateDao,
    private val notificationHelper: NotificationHelper
) {
    suspend operator fun invoke() {
        val today = LocalDate.now()
        val dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Only care about categories that have limits
        val allCategories = categoryDao.getAllCategories().firstOrNull() ?: emptyList()
        val limitedCategories = allCategories.filter { it.dailyLimitMinutes != null }

        if (limitedCategories.isEmpty()) return

        val manualLogs = activityLogDao.getActivitiesByDate(dateString).firstOrNull() ?: emptyList()
        val autoUsage = usageAggregateDao.getUsageByDate(dateString).firstOrNull() ?: emptyList()

        for (category in limitedCategories) {
            val limit = category.dailyLimitMinutes ?: continue

            // Sum duration
            val manualSum = manualLogs.filter { it.categoryId == category.id }.sumOf { it.durationMinutes }
            val autoSum = autoUsage.filter { it.categoryId == category.id }.sumOf { it.foregroundMinutes }
            val totalMinutes = manualSum + autoSum

            if (totalMinutes == 0) continue

            // Evaluate 100% threshold
            if (totalMinutes >= limit) {
                checkAndNotify(dateString, category.id, category.name, 100)
            }
            // Evaluate 80% threshold
            else if (totalMinutes >= (limit * 0.8).toInt()) {
                checkAndNotify(dateString, category.id, category.name, 80)
            }
        }
    }

    private suspend fun checkAndNotify(dateString: String, categoryId: Long, categoryName: String, threshold: Int) {
        val hasNotified = notificationStateDao.hasNotified(dateString, categoryId, threshold)
        if (!hasNotified) {
            // Fire notification
            notificationHelper.showLimitNotification(categoryId, categoryName, threshold)

            // Mark as notified
            notificationStateDao.insertState(
                NotificationStateEntity(
                    date = dateString,
                    categoryId = categoryId,
                    thresholdType = threshold,
                    notifiedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
