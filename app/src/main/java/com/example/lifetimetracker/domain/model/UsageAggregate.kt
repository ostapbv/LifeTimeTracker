package com.example.lifetimetracker.domain.model

data class UsageAggregate(
    val id: Long = 0,
    val date: String,
    val packageName: String,
    val appLabel: String?,
    val categoryId: Long,
    val foregroundMinutes: Int,
    val lastSyncedAt: Long
)
