package com.example.lifetimetracker.data.repository

import com.example.lifetimetracker.data.local.dao.CategoryDao
import com.example.lifetimetracker.data.local.dao.UsageAggregateDao
import com.example.lifetimetracker.data.local.entity.UsageAggregateEntity
import com.example.lifetimetracker.data.system.UsageStatsDataSource
import com.example.lifetimetracker.domain.model.UsageAggregate
import com.example.lifetimetracker.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val usageAggregateDao: UsageAggregateDao,
    private val categoryDao: CategoryDao,
    private val usageStatsDataSource: UsageStatsDataSource
) : UsageRepository {

    override fun hasUsagePermission(): Boolean {
        return usageStatsDataSource.hasUsagePermission()
    }

    override suspend fun syncUsageForDate(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val rawStats = usageStatsDataSource.getUsageStatsForDate(date)

        val allCategories = categoryDao.getAllCategories().firstOrNull() ?: emptyList()
        val defaultOtherCategoryId = allCategories.find { it.key == "other" }?.id ?: 0L

        val currentTime = System.currentTimeMillis()

        for (raw in rawStats) {
            val category = allCategories.find { it.key == raw.categoryKey }
            val categoryId = category?.id ?: defaultOtherCategoryId

            val entity = UsageAggregateEntity(
                date = dateString,
                packageName = raw.packageName,
                appLabel = raw.appLabel,
                categoryId = categoryId,
                foregroundMinutes = raw.foregroundMinutes,
                lastSyncedAt = currentTime
            )
            usageAggregateDao.upsert(entity)
        }
    }

    override fun getUsageByDate(date: String): Flow<List<UsageAggregate>> {
        return usageAggregateDao.getUsageByDate(date).map { entities ->
            entities.map {
                UsageAggregate(
                    id = it.id,
                    date = it.date,
                    packageName = it.packageName,
                    appLabel = it.appLabel,
                    categoryId = it.categoryId,
                    foregroundMinutes = it.foregroundMinutes,
                    lastSyncedAt = it.lastSyncedAt
                )
            }
        }
    }
}
