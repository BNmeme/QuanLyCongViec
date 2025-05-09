package com.example.quanlycongviec.ui.screens.tasks.personal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class PersonalTaskDetailViewModel(
    private val taskId: String
) : ViewModel() {
    private val taskRepository = AppModule.provideTaskRepository()
    private val labelRepository = AppModule.provideLabelRepository()
    private val authRepository = AppModule.provideAuthRepository()

    private val _uiState = MutableStateFlow(PersonalTaskDetailUiState())
    val uiState: StateFlow<PersonalTaskDetailUiState> = _uiState.asStateFlow()

    init {
        loadTask()
        loadLabels()
    }

    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val task = taskRepository.getTaskById(taskId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        task = task
                    )
                }

                // Load labels for this task
                if (task.labels.isNotEmpty()) {
                    val taskLabels = labelRepository.getLabelsByIds(task.labels)
                    _uiState.update {
                        it.copy(taskLabels = taskLabels)
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalTaskDetailVM", "Error loading task", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load task: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadLabels() {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                val labels = labelRepository.getLabelsForUser(currentUserId)
                _uiState.update {
                    it.copy(availableLabels = labels)
                }
            } catch (e: Exception) {
                Log.e("PersonalTaskDetailVM", "Error loading labels", e)
            }
        }
    }

    fun toggleTaskCompletion(isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskCompletion(taskId, isCompleted)

                // Update local state
                _uiState.update {
                    it.copy(
                        task = it.task?.copy(isCompleted = isCompleted)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update task: ${e.message}")
                }
            }
        }
    }

    fun showDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = true) }
    }

    fun hideDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    fun deleteTask() {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmationDialog = false,
                        navigateBack = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to delete task: ${e.message}",
                        showDeleteConfirmationDialog = false
                    )
                }
            }
        }
    }

    fun addLabelToTask(labelId: String) {
        viewModelScope.launch {
            try {
                val currentTask = _uiState.value.task ?: return@launch
                val updatedLabelIds = currentTask.labels.toMutableList()

                if (!updatedLabelIds.contains(labelId)) {
                    updatedLabelIds.add(labelId)

                    val updatedTask = currentTask.copy(labels = updatedLabelIds)
                    taskRepository.updateTask(updatedTask)

                    // Update UI state
                    val label = _uiState.value.availableLabels.find { it.id == labelId }
                    if (label != null) {
                        val updatedLabels = _uiState.value.taskLabels.toMutableList()
                        updatedLabels.add(label)

                        _uiState.update {
                            it.copy(
                                task = updatedTask,
                                taskLabels = updatedLabels
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalTaskDetailVM", "Error adding label to task", e)
                _uiState.update {
                    it.copy(errorMessage = "Failed to add label: ${e.message}")
                }
            }
        }
    }

    fun removeLabelFromTask(labelId: String) {
        viewModelScope.launch {
            try {
                val currentTask = _uiState.value.task ?: return@launch
                val updatedLabelIds = currentTask.labels.toMutableList()

                if (updatedLabelIds.contains(labelId)) {
                    updatedLabelIds.remove(labelId)

                    val updatedTask = currentTask.copy(labels = updatedLabelIds)
                    taskRepository.updateTask(updatedTask)

                    // Update UI state
                    val updatedLabels = _uiState.value.taskLabels.filter { it.id != labelId }

                    _uiState.update {
                        it.copy(
                            task = updatedTask,
                            taskLabels = updatedLabels
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalTaskDetailVM", "Error removing label from task", e)
                _uiState.update {
                    it.copy(errorMessage = "Failed to remove label: ${e.message}")
                }
            }
        }
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
                loadLabels()

                // Auto-add the new label to the task
                addLabelToTask(labelId)
            } catch (e: Exception) {
                Log.e("PersonalTaskDetailVM", "Error creating label", e)
                _uiState.update {
                    it.copy(errorMessage = "Failed to create label: ${e.message}")
                }
            }
        }
    }
}

data class PersonalTaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val taskLabels: List<Label> = emptyList(),
    val availableLabels: List<Label> = emptyList(),
    val errorMessage: String? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val navigateBack: Boolean = false
)

class PersonalTaskDetailViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalTaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonalTaskDetailViewModel(taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
