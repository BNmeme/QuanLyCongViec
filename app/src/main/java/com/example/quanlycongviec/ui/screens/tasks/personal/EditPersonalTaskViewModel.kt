package com.example.quanlycongviec.ui.screens.tasks.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.LabelRepository
import com.example.quanlycongviec.data.repository.NotificationRepository
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

class EditPersonalTaskViewModel(
    private val taskId: String?
) : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val taskRepository = AppModule.provideTaskRepository()
    private val labelRepository = AppModule.provideLabelRepository()
    private val notificationRepository = AppModule.provideNotificationRepository()
    
    private val _uiState = MutableStateFlow(EditPersonalTaskUiState())
    val uiState: StateFlow<EditPersonalTaskUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                
                // Load available labels
                val labels = labelRepository.getLabelsForUser(currentUserId)
                
                if (taskId != null) {
                    // Load existing task
                    val task = taskRepository.getTaskById(taskId)
                    
                    // Load selected labels
                    val selectedLabels = if (task.labels.isNotEmpty()) {
                        labelRepository.getLabelsByIds(task.labels)
                    } else {
                        emptyList()
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            title = task.title,
                            description = task.description,
                            dueDate = task.dueDate,
                            priority = task.priority,
                            availableLabels = labels,
                            selectedLabels = selectedLabels
                        ) 
                    }
                } else {
                    // Set default due date to tomorrow
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 9)
                    calendar.set(Calendar.MINUTE, 0)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            dueDate = calendar.timeInMillis,
                            availableLabels = labels
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load data: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.update { 
            it.copy(
                title = title,
                titleError = if (title.isBlank()) "Title cannot be empty" else null
            ) 
        }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun updateDueDate(dueDate: Long) {
        _uiState.update { it.copy(dueDate = dueDate) }
    }
    
    fun updatePriority(priority: Int) {
        _uiState.update { it.copy(priority = priority) }
    }
    
    fun addLabel(labelId: String) {
        viewModelScope.launch {
            try {
                val label = uiState.value.availableLabels.find { it.id == labelId } ?: return@launch
                
                val currentSelectedLabels = _uiState.value.selectedLabels.toMutableList()
                if (!currentSelectedLabels.any { it.id == labelId }) {
                    currentSelectedLabels.add(label)
                }
                
                _uiState.update { it.copy(selectedLabels = currentSelectedLabels) }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun removeLabel(labelId: String) {
        val currentSelectedLabels = _uiState.value.selectedLabels.toMutableList()
        currentSelectedLabels.removeAll { it.id == labelId }
        
        _uiState.update { it.copy(selectedLabels = currentSelectedLabels) }
    }
    
    fun createLabel(name: String, color: String) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                
                val newLabel = Label(
                    name = name,
                    color = color,
                    userId = currentUserId
                )
                
                val labelId = labelRepository.createLabel(newLabel)
                
                // Refresh available labels
                val updatedLabels = labelRepository.getLabelsForUser(currentUserId)
                
                _uiState.update { 
                    it.copy(availableLabels = updatedLabels) 
                }
                
                // Auto-select the newly created label
                val createdLabel = updatedLabels.find { it.id == labelId }
                if (createdLabel != null) {
                    val currentSelectedLabels = _uiState.value.selectedLabels.toMutableList()
                    currentSelectedLabels.add(createdLabel)
                    _uiState.update { it.copy(selectedLabels = currentSelectedLabels) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to create label: ${e.message}") 
                }
            }
        }
    }
    
    fun saveTask() {
        if (uiState.value.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                
                val task = Task(
                    id = taskId ?: "",
                    title = uiState.value.title,
                    description = uiState.value.description,
                    dueDate = uiState.value.dueDate,
                    priority = uiState.value.priority,
                    userId = currentUserId,
                    labels = uiState.value.selectedLabels.map { it.id }
                )
                
                if (taskId == null) {
                    // Create new task
                    taskRepository.createTask(task)
                    
                    // Create deadline notification
                    scheduleDeadlineNotification(task)
                } else {
                    // Update existing task
                    taskRepository.updateTask(task)
                }
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        isTaskSaved = true
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save task: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun deleteTask(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                taskId?.let { id ->
                    taskRepository.deleteTask(id)
                    _uiState.update { it.copy(isSaving = false) }
                    onComplete()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to delete task: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private suspend fun scheduleDeadlineNotification(task: Task) {
        try {
            val currentTime = System.currentTimeMillis()
            val timeUntilDeadline = task.dueDate - currentTime
            
            // Only schedule if deadline is in the future
            if (timeUntilDeadline > 0) {
                notificationRepository.createTaskDeadlineNotification(
                    userId = task.userId,
                    taskTitle = task.title,
                    taskId = task.id,
                    dueDate = task.dueDate
                )
            }
        } catch (e: Exception) {
            // Handle silently - notification creation should not block task creation
        }
    }
}

data class EditPersonalTaskUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isTaskSaved: Boolean = false,
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val priority: Int = 3, // 1: High, 2: Medium, 3: Low
    val availableLabels: List<Label> = emptyList(),
    val selectedLabels: List<Label> = emptyList(),
    val errorMessage: String? = null
)

class EditPersonalTaskViewModelFactory(private val taskId: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPersonalTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditPersonalTaskViewModel(taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
