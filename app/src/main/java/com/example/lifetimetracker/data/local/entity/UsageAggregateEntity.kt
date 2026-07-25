package com.example.lifetimetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_aggregates",
    indices = [
        Index(value = ["date", "packageName"], unique = true)
    ]
)
data class UsageAggregateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,              // ISO local date: YYYY-MM-DD
    val packageName: String,
    val appLabel: String?,
    val categoryId: Long,          // Mapped category ID (can be the system default 'other')
    val foregroundMinutes: Int,
    val lastSyncedAt: Long
)
