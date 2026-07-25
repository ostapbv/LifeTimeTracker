package com.example.lifetimetracker.domain.repository

import com.example.lifetimetracker.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    suspend fun insertActivityLog(activityLog: ActivityLog): Long
    suspend fun updateActivityLog(activityLog: ActivityLog)
    suspend fun deleteActivityLog(activityLog: ActivityLog)
    
    fun getActivitiesByDateRange(startDate: String, endDate: String): Flow<List<ActivityLog>>
    fun getActivitiesByDate(date: String): Flow<List<ActivityLog>>
    fun getTotalDurationByCategoryForDate(categoryId: Long, date: String): Flow<Int?>
}
