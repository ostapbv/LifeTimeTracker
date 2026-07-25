package com.example.lifetimetracker.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifetimetracker.domain.model.ActivityLog
import com.example.lifetimetracker.domain.model.Category
import com.example.lifetimetracker.domain.repository.ActivityRepository
import com.example.lifetimetracker.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import com.example.lifetimetracker.domain.model.UsageAggregate
import com.example.lifetimetracker.domain.repository.UsageRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val categoryRepository: CategoryRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate

    private val _hasUsagePermission = MutableStateFlow(usageRepository.hasUsagePermission())
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayLogs: StateFlow<List<ActivityLog>> = _currentDate
        .map { it.format(DateTimeFormatter.ISO_LOCAL_DATE) }
        .flatMapLatest { dateString ->
            activityRepository.getActivitiesByDate(dateString)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayUsageAggregates: StateFlow<List<UsageAggregate>> = _currentDate
        .map { it.format(DateTimeFormatter.ISO_LOCAL_DATE) }
        .flatMapLatest { dateString ->
            usageRepository.getUsageByDate(dateString)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadDataForDate(date: LocalDate) {
        _currentDate.value = date
        checkUsagePermission()
    }

    fun checkUsagePermission() {
        val granted = usageRepository.hasUsagePermission()
        _hasUsagePermission.value = granted
        if (granted) {
            syncUsageData()
        }
    }

    private fun syncUsageData() {
        viewModelScope.launch {
            try {
                usageRepository.syncUsageForDate(_currentDate.value)
            } catch (e: Exception) {
                // Ignore sync errors for now (best-effort)
            }
        }
    }
}
