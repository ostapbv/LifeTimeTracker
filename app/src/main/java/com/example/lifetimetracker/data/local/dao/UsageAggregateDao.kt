package com.example.lifetimetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lifetimetracker.data.local.entity.UsageAggregateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageAggregateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(usageAggregate: UsageAggregateEntity)

    @Query("SELECT * FROM usage_aggregates WHERE date = :date")
    fun getUsageByDate(date: String): Flow<List<UsageAggregateEntity>>

    @Query("SELECT * FROM usage_aggregates WHERE date >= :startDate AND date <= :endDate")
    fun getUsageByDateRange(startDate: String, endDate: String): Flow<List<UsageAggregateEntity>>
}
