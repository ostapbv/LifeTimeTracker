package com.example.lifetimetracker.domain.model

data class ActivityLog(
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
