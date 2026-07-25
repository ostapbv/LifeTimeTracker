package com.example.lifetimetracker.presentation.screen.logactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifetimetracker.domain.model.ActivityLog
import com.example.lifetimetracker.domain.model.ActivitySource
import com.example.lifetimetracker.domain.model.Category
import com.example.lifetimetracker.domain.repository.ActivityRepository
import com.example.lifetimetracker.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import com.example.lifetimetracker.domain.parser.ActivityTextParser

import com.example.lifetimetracker.domain.usecase.LimitEvaluationUseCase

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    categoryRepository: CategoryRepository,
    private val textParser: ActivityTextParser,
    private val limitEvaluationUseCase: LimitEvaluationUseCase
) : ViewModel() {

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _durationMinutes = MutableStateFlow("")
    val durationMinutes: StateFlow<String> = _durationMinutes

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note
    
    private val _isVoiceMode = MutableStateFlow(false)
    val isVoiceMode: StateFlow<Boolean> = _isVoiceMode

    fun onCategorySelected(category: Category) {
        _selectedCategory.value = category
    }

    fun onDurationChanged(duration: String) {
        if (duration.isEmpty() || duration.toIntOrNull() != null) {
            _durationMinutes.value = duration
        }
    }

    fun onNoteChanged(newNote: String) {
        _note.value = newNote
    }
    
    fun processVoiceInput(text: String) {
        _note.value = text
        val parsed = textParser.parse(text)
        
        parsed.durationMinutes?.let { duration ->
            _durationMinutes.value = duration.toString()
        }
        
        parsed.categoryKey?.let { key ->
            val cat = categories.value.find { it.key == key }
            if (cat != null) {
                _selectedCategory.value = cat
            }
        }
    }
    
    fun setVoiceMode(isActive: Boolean) {
        _isVoiceMode.value = isActive
    }

    fun saveActivity(onSuccess: () -> Unit) {
        val category = _selectedCategory.value ?: return
        val duration = _durationMinutes.value.toIntOrNull() ?: return
        if (duration <= 0) return
        
        val source = if (_isVoiceMode.value) ActivitySource.VOICE else ActivitySource.MANUAL

        viewModelScope.launch {
            val log = ActivityLog(
                categoryId = category.id,
                startTime = System.currentTimeMillis(), // Simplified
                endTime = System.currentTimeMillis(), // Simplified
                durationMinutes = duration,
                source = source,
                note = _note.value.ifBlank { null },
                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            activityRepository.insertActivityLog(log)
            
            // Check limits right after adding a manual log
            limitEvaluationUseCase()
            
            onSuccess()
        }
    }
}
