package com.example.lifetimetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifetimetracker.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(activityLog: ActivityLogEntity): Long

    @Update
    suspend fun updateActivityLog(activityLog: ActivityLogEntity)

    @Delete
    suspend fun deleteActivityLog(activityLog: ActivityLogEntity)

    @Query("SELECT * FROM activity_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getActivitiesByDateRange(startDate: String, endDate: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE date = :date ORDER BY startTime DESC")
    fun getActivitiesByDate(date: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT SUM(durationMinutes) FROM activity_logs WHERE categoryId = :categoryId AND date = :date")
    fun getTotalDurationByCategoryForDate(categoryId: Long, date: String): Flow<Int?>
}
