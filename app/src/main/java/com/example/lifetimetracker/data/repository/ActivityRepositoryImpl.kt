package com.example.lifetimetracker.data.repository

import com.example.lifetimetracker.data.local.dao.ActivityLogDao
import com.example.lifetimetracker.data.local.entity.ActivityLogEntity
import com.example.lifetimetracker.domain.model.ActivityLog
import com.example.lifetimetracker.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityLogDao: ActivityLogDao
) : ActivityRepository {

    override suspend fun insertActivityLog(activityLog: ActivityLog): Long {
        return activityLogDao.insertActivityLog(activityLog.toEntity())
    }

    override suspend fun updateActivityLog(activityLog: ActivityLog) {
        activityLogDao.updateActivityLog(activityLog.toEntity())
    }

    override suspend fun deleteActivityLog(activityLog: ActivityLog) {
        activityLogDao.deleteActivityLog(activityLog.toEntity())
    }

    override fun getActivitiesByDateRange(startDate: String, endDate: String): Flow<List<ActivityLog>> {
        return activityLogDao.getActivitiesByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActivitiesByDate(date: String): Flow<List<ActivityLog>> {
        return activityLogDao.getActivitiesByDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalDurationByCategoryForDate(categoryId: Long, date: String): Flow<Int?> {
        return activityLogDao.getTotalDurationByCategoryForDate(categoryId, date)
    }

    private fun ActivityLog.toEntity(): ActivityLogEntity {
        return ActivityLogEntity(
            id = id,
            categoryId = categoryId,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            source = source,
            note = note,
            date = date,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun ActivityLogEntity.toDomain(): ActivityLog {
        return ActivityLog(
            id = id,
            categoryId = categoryId,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            source = source,
            note = note,
            date = date,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
