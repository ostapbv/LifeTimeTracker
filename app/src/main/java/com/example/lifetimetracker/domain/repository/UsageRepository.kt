package com.example.lifetimetracker.domain.repository

import com.example.lifetimetracker.domain.model.UsageAggregate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface UsageRepository {
    fun hasUsagePermission(): Boolean
    suspend fun syncUsageForDate(date: LocalDate)
    fun getUsageByDate(date: String): Flow<List<UsageAggregate>>
}
