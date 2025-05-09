package com.example.quanlycongviec.ui.screens.tasks.personal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.LabelRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Label
import com.example.quanlycongviec.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class TaskFilter {
    ALL,
    COMPLETED,
    PENDING,
    HIGH_PRIORITY,
    MEDIUM_PRIORITY,
    LOW_PRIORITY,
    DUE_TODAY,
    DUE_THIS_WEEK,
    BY_LABEL
}

class PersonalTasksViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val taskRepository = AppModule.provideTaskRepository()
    private val labelRepository = AppModule.provideLabelRepository()

    private val _uiState = MutableStateFlow(PersonalTasksUiState())
    val uiState: StateFlow<PersonalTasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
        loadLabels()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                Log.d("PersonalTasksViewModel", "Current user ID: $currentUserId")

                if (currentUserId != null) {
                    val tasks = taskRepository.getPersonalTasks(currentUserId)
                    Log.d("PersonalTasksViewModel", "Loaded ${tasks.size} tasks")

                    _uiState.update { currentState ->
                        val sortedTasks = tasks.sortedByDescending { task -> task.createdAt }
                        currentState.copy(
                            isLoading = false,
                            tasks = sortedTasks,
                            filteredTasks = applyFilters(sortedTasks, currentState.activeFilter, currentState.selectedLabelId)
                        )
                    }
                } else {
                    Log.e("PersonalTasksViewModel", "User not logged in")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User not logged in"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalTasksViewModel", "Error loading tasks", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load tasks: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadLabels() {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val labels = labelRepository.getLabelsForUser(currentUserId)
                    _uiState.update { it.copy(availableLabels = labels) }
                }
            } catch (e: Exception) {
                Log.e("PersonalTasksViewModel", "Error loading labels", e)
            }
        }
    }

    fun addTask(task: Task, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentUserId = authRepository.getCurrentUserId()
                Log.d("PersonalTasksViewModel", "Adding task for user: $currentUserId")

                if (currentUserId != null) {
                    // Create a copy of the task with the current user ID
                    val taskWithUserId = task.copy(
                        userId = currentUserId,
                        createdAt = System.currentTimeMillis()
                    )
                    Log.d("PersonalTasksViewModel", "Task to create: $taskWithUserId")
                    val taskId = taskRepository.createTask(taskWithUserId)
                    Log.d("PersonalTasksViewModel", "Task created with ID: $taskId")

                    // Reload tasks after adding
                    loadTasks()
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete()
                } else {
                    Log.e("PersonalTasksViewModel", "Cannot add task: User not logged in")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "User not logged in")
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalTasksViewModel", "Error adding task", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to add task: ${e.message}")
                }
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                activeFilter = filter,
                filteredTasks = applyFilters(currentState.tasks, filter, currentState.selectedLabelId),
                isFilterMenuOpen = false
            )
        }
    }

    fun setLabelFilter(labelId: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedLabelId = labelId,
                activeFilter = if (labelId != null) TaskFilter.BY_LABEL else currentState.activeFilter,
                filteredTasks = applyFilters(currentState.tasks,
                    if (labelId != null) TaskFilter.BY_LABEL else currentState.activeFilter,
                    labelId),
                isLabelFilterMenuOpen = false
            )
        }
    }

    private fun applyFilters(tasks: List<Task>, filter: TaskFilter, labelId: String?): List<Task> {
        // First apply the main filter
        val filteredByType = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.HIGH_PRIORITY -> tasks.filter { it.priority == 1 }
            TaskFilter.MEDIUM_PRIORITY -> tasks.filter { it.priority == 2 }
            TaskFilter.LOW_PRIORITY -> tasks.filter { it.priority == 3 }
            TaskFilter.DUE_TODAY -> {
                val calendar = Calendar.getInstance()
                val startOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                tasks.filter { it.dueDate in startOfDay..endOfDay }
            }
            TaskFilter.DUE_THIS_WEEK -> {
                val calendar = Calendar.getInstance()
                val today = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfWeek = calendar.apply {
                    add(Calendar.DAY_OF_YEAR, 7)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                tasks.filter { it.dueDate in today..endOfWeek }
            }
            TaskFilter.BY_LABEL -> tasks
        }

        // Then apply label filter if needed
        return if (labelId != null) {
            filteredByType.filter { it.labels.contains(labelId) }
        } else {
            filteredByType
        }
    }

    fun toggleFilterMenu() {
        _uiState.update { it.copy(isFilterMenuOpen = !it.isFilterMenuOpen, isLabelFilterMenuOpen = false) }
    }

    fun toggleLabelFilterMenu() {
        _uiState.update { it.copy(isLabelFilterMenuOpen = !it.isLabelFilterMenuOpen, isFilterMenuOpen = false) }
    }

    fun closeFilterMenus() {
        _uiState.update { it.copy(isFilterMenuOpen = false, isLabelFilterMenuOpen = false) }
    }

    fun showAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = true) }
    }

    fun hideAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = false) }
    }

    fun refreshTasks() {
        loadTasks()
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                // First update the local state immediately for responsive UI
                _uiState.update { currentState ->
                    val updatedTasks = currentState.tasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
                    }

                    currentState.copy(
                        tasks = updatedTasks,
                        filteredTasks = applyFilters(updatedTasks, currentState.activeFilter, currentState.selectedLabelId)
                    )
                }

                // Then update the database
                taskRepository.toggleTaskCompletion(taskId, isCompleted)

                // Log the update
                Log.d("PersonalTasksViewModel", "Task $taskId completion toggled to $isCompleted")

            } catch (e: Exception) {
                // If there's an error, revert the local state and show error
                Log.e("PersonalTasksViewModel", "Error toggling task completion", e)
                loadTasks() // Reload from database to ensure consistency
                _uiState.update {
                    it.copy(errorMessage = "Failed to update task: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearAllFilters() {
        _uiState.update { currentState ->
            currentState.copy(
                activeFilter = TaskFilter.ALL,
                selectedLabelId = null,
                filteredTasks = currentState.tasks,
                isFilterMenuOpen = false,
                isLabelFilterMenuOpen = false
            )
        }
    }
}

data class PersonalTasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val activeFilter: TaskFilter = TaskFilter.ALL,
    val selectedLabelId: String? = null,
    val availableLabels: List<Label> = emptyList(),
    val showAddTaskDialog: Boolean = false,
    val errorMessage: String? = null,
    val isFilterMenuOpen: Boolean = false,
    val isLabelFilterMenuOpen: Boolean = false
)
