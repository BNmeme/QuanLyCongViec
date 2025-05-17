package com.example.quanlycongviec.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class CalendarViewModel : ViewModel() {
    private val taskRepository = AppModule.provideTaskRepository()
    private val authRepository = AppModule.provideAuthRepository()

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var allTasks: List<Task> = emptyList()
    private var tasksByDate: Map<Long, List<Task>> = emptyMap()

    init {
        val calendar = Calendar.getInstance()
        _uiState.update {
            it.copy(
                currentMonth = calendar.get(Calendar.MONTH),
                currentYear = calendar.get(Calendar.YEAR),
                selectedDate = calendar.timeInMillis
            )
        }
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User not authenticated"
                        )
                    }
                    return@launch
                }

                // Get all tasks for the current user
                val personalTasks = taskRepository.getPersonalTasks(userId)
                val groupTasks = taskRepository.getGroupTasksForUser(userId)

                allTasks = personalTasks + groupTasks

                // Group tasks by date (normalized to start of day)
                tasksByDate = allTasks.groupBy { task ->
                    normalizeToStartOfDay(task.dueDate)
                }

                // Update tasks for selected date
                updateTasksForSelectedDate()

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load tasks: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectDate(date: Long) {
        _uiState.update { it.copy(selectedDate = date) }
        updateTasksForSelectedDate()
    }

    fun previousMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, _uiState.value.currentYear)
        calendar.set(Calendar.MONTH, _uiState.value.currentMonth)
        calendar.add(Calendar.MONTH, -1)

        _uiState.update {
            it.copy(
                currentMonth = calendar.get(Calendar.MONTH),
                currentYear = calendar.get(Calendar.YEAR)
            )
        }
    }

    fun nextMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, _uiState.value.currentYear)
        calendar.set(Calendar.MONTH, _uiState.value.currentMonth)
        calendar.add(Calendar.MONTH, 1)

        _uiState.update {
            it.copy(
                currentMonth = calendar.get(Calendar.MONTH),
                currentYear = calendar.get(Calendar.YEAR)
            )
        }
    }

    fun hasTasksForDate(date: Long): Boolean {
        val normalizedDate = normalizeToStartOfDay(date)
        return tasksByDate[normalizedDate]?.isNotEmpty() == true
    }

    private fun updateTasksForSelectedDate() {
        val normalizedSelectedDate = normalizeToStartOfDay(_uiState.value.selectedDate)
        val tasksForDate = tasksByDate[normalizedSelectedDate] ?: emptyList()

        _uiState.update { it.copy(tasksForSelectedDate = tasksForDate) }
    }

    private fun normalizeToStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

data class CalendarUiState(
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedDate: Long = 0,
    val tasksForSelectedDate: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
