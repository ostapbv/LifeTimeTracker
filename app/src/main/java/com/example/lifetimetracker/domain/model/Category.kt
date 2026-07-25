package com.example.lifetimetracker.domain.model

data class Category(
    val id: Long,
    val key: String,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val dailyLimitMinutes: Int?,
    val isSystem: Boolean,
    val sortOrder: Int
)