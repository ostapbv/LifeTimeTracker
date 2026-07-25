package com.example.lifetimetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_states",
    indices = [
        Index(value = ["date", "categoryId", "thresholdType"], unique = true)
    ]
)
data class NotificationStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,             // ISO local date: YYYY-MM-DD
    val categoryId: Long,
    val thresholdType: Int,       // 80 or 100
    val notifiedAt: Long
)
