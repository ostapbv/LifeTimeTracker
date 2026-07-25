package com.example.lifetimetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val dailyLimitMinutes: Int?,
    val isSystem: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long
)
