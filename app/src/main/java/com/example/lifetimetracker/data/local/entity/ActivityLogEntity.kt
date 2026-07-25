package com.example.lifetimetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lifetimetracker.domain.model.ActivitySource

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val source: ActivitySource,
    val note: String?,
    val date: String, // ISO local date: YYYY-MM-DD
    val createdAt: Long,
    val updatedAt: Long
)
