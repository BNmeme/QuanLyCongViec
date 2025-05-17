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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUserId = authRepository.getCurrentUserId()

                if (currentUserId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User not logged in"
                        )
                    }
                    return@launch
                }

                // Load all tasks
                val personalTasks = try {
                    taskRepository.getPersonalTasks(currentUserId)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load personal tasks: ${e.message}"
                        )
                    }
                    return@launch
                }

                val groupTasks = try {
                    taskRepository.getGroupTasksForUser(currentUserId)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load group tasks: ${e.message}"
                        )
                    }
                    return@launch
                }

                val allTasks = personalTasks + groupTasks

                // Calculate basic statistics
                val totalTasks = allTasks.size
                val completedTasks = allTasks.count { it.isCompleted }

                val highPriorityCount = allTasks.count { it.priority == 1 }
                val mediumPriorityCount = allTasks.count { it.priority == 2 }
                val lowPriorityCount = allTasks.count { it.priority == 3 }

                // Calculate completed tasks by priority
                val completedHighPriorityCount = allTasks.count { it.priority == 1 && it.isCompleted }
                val completedMediumPriorityCount = allTasks.count { it.priority == 2 && it.isCompleted }
                val completedLowPriorityCount = allTasks.count { it.priority == 3 && it.isCompleted }

                // Calculate on-time completion rate
                val tasksWithDueDate = allTasks.filter { it.dueDate > 0 }
                val totalTasksWithDueDate = tasksWithDueDate.size

                val onTimeCompletedTasks = tasksWithDueDate.count { task ->
                    // Since we don't have completedAt timestamp, we'll consider all completed tasks with future due dates as on-time
                    task.isCompleted && task.dueDate >= System.currentTimeMillis()
                }

                val onTimeCompletionRate = if (tasksWithDueDate.isNotEmpty()) {
                    (onTimeCompletedTasks.toFloat() / tasksWithDueDate.size) * 100
                } else {
                    100f // If no tasks with due dates, consider it 100%
                }

                // Calculate overdue tasks
                val overdueTasksCount = tasksWithDueDate.count {
                    !it.isCompleted && it.dueDate < System.currentTimeMillis()
                }

                // Calculate activity by day of week
                val calendar = Calendar.getInstance()
                val activityByDayOfWeek = mutableMapOf<Int, Int>()

                // Initialize all days to 0
                for (i in 0..6) {
                    activityByDayOfWeek[i] = 0
                }

                // Count tasks by day of week
                allTasks.forEach { task ->
                    try {
                        calendar.timeInMillis = task.createdAt
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 6 = Saturday
                        activityByDayOfWeek[dayOfWeek] = (activityByDayOfWeek[dayOfWeek] ?: 0) + 1
                    } catch (e: Exception) {
                        // Skip this task if there's an error
                    }
                }

                // Calculate average completion time (in hours)
                // Since we don't have completedAt, we'll use a placeholder value
                val averageCompletionTime = 48f // Placeholder: 48 hours

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        highPriorityCount = highPriorityCount,
                        mediumPriorityCount = mediumPriorityCount,
                        lowPriorityCount = lowPriorityCount,
                        completedHighPriorityCount = completedHighPriorityCount,
                        completedMediumPriorityCount = completedMediumPriorityCount,
                        completedLowPriorityCount = completedLowPriorityCount,
                        personalTaskCount = personalTasks.size,
                        groupTaskCount = groupTasks.size,
                        onTimeCompletionRate = onTimeCompletionRate,
                        activityByDayOfWeek = activityByDayOfWeek,
                        averageCompletionTime = averageCompletionTime,
                        overdueTasksCount = overdueTasksCount,
                        totalTasksWithDueDate = totalTasksWithDueDate
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

    fun refreshStatistics() {
        loadStatistics()
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val highPriorityCount: Int = 0,
    val mediumPriorityCount: Int = 0,
    val lowPriorityCount: Int = 0,
    val completedHighPriorityCount: Int = 0,
    val completedMediumPriorityCount: Int = 0,
    val completedLowPriorityCount: Int = 0,
    val personalTaskCount: Int = 0,
    val groupTaskCount: Int = 0,
    val onTimeCompletionRate: Float = 0f,
    val activityByDayOfWeek: Map<Int, Int> = emptyMap(),
    val averageCompletionTime: Float = 0f,
    val overdueTasksCount: Int = 0,
    val totalTasksWithDueDate: Int = 0,
    val errorMessage: String? = null
)
