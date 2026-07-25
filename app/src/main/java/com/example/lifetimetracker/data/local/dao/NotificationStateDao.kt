package com.example.lifetimetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lifetimetracker.data.local.entity.NotificationStateEntity

@Dao
interface NotificationStateDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertState(state: NotificationStateEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM notification_states WHERE date = :date AND categoryId = :categoryId AND thresholdType = :thresholdType LIMIT 1)")
    suspend fun hasNotified(date: String, categoryId: Long, thresholdType: Int): Boolean
}
