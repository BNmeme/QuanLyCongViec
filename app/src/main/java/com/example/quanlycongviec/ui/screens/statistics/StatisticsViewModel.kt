package com.example.quanlycongviec.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val taskRepository = AppModule.provideTaskRepository()
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                
                // Load all tasks
                val personalTasks = taskRepository.getPersonalTasks(currentUserId)
                val groupTasks = taskRepository.getGroupTasksForUser(currentUserId)
                
                val allTasks = personalTasks + groupTasks
                
                // Calculate statistics
                val totalTasks = allTasks.size
                val completedTasks = allTasks.count { it.isCompleted }
                
                val highPriorityCount = allTasks.count { it.priority == 1 }
                val mediumPriorityCount = allTasks.count { it.priority == 2 }
                val lowPriorityCount = allTasks.count { it.priority == 3 }
                
                // Group tasks by creation date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val tasksByDay = allTasks.groupBy { 
                    dateFormat.format(Date(it.createdAt))
                }.mapValues { it.value.size }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        highPriorityCount = highPriorityCount,
                        mediumPriorityCount = mediumPriorityCount,
                        lowPriorityCount = lowPriorityCount,
                        personalTaskCount = personalTasks.size,
                        groupTaskCount = groupTasks.size,
                        taskCountByDay = tasksByDay
                    ) 
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load statistics: ${e.message}"
                    ) 
                }
            }
        }
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val highPriorityCount: Int = 0,
    val mediumPriorityCount: Int = 0,
    val lowPriorityCount: Int = 0,
    val personalTaskCount: Int = 0,
    val groupTaskCount: Int = 0,
    val taskCountByDay: Map<String, Int> = emptyMap(),
    val errorMessage: String? = null
)
