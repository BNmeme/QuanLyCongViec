package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupTaskDetailViewModel(
    private val taskId: String
) : ViewModel() {
    private val taskRepository = AppModule.provideTaskRepository()
    private val userRepository = AppModule.provideUserRepository()
    private val groupRepository = AppModule.provideGroupRepository()
    
    private val _uiState = MutableStateFlow(GroupTaskDetailUiState())
    val uiState: StateFlow<GroupTaskDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadTask()
    }
    
    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val task = taskRepository.getTaskById(taskId)
                
                // Load assigned users
                val assignedUsers = if (task.assignedTo.isNotEmpty()) {
                    userRepository.getUsersByIds(task.assignedTo)
                } else {
                    emptyList()
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        task = task,
                        assignedUsers = assignedUsers
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load task: ${e.message}"
                    ) 
                }
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
    
    fun deleteTask(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
                onSuccess()
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
    
    fun showEditTaskDialog() {
        _uiState.update { it.copy(showEditTaskDialog = true) }
    }
    
    fun hideEditTaskDialog() {
        _uiState.update { it.copy(showEditTaskDialog = false) }
    }

    fun showAssignmentDialog() {
        viewModelScope.launch {
            try {
                // Get the group associated with this task
                val task = _uiState.value.task ?: return@launch
                if (!task.isGroupTask || task.groupId.isEmpty()) return@launch

                val group = groupRepository.getGroupById(task.groupId)
                val members = userRepository.getUsersByIds(group.members)

                _uiState.update {
                    it.copy(
                        showAssignmentDialog = true,
                        groupMembers = members
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to load group members: ${e.message}")
                }
            }
        }
    }

    fun hideAssignmentDialog() {
        _uiState.update { it.copy(showAssignmentDialog = false) }
    }

    fun reassignTask(assignedTo: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReassigningTask = true) }

            try {
                taskRepository.reassignTask(taskId, assignedTo)

                // Update local state
                _uiState.update {
                    it.copy(
                        isReassigningTask = false,
                        showAssignmentDialog = false,
                        task = it.task?.copy(assignedTo = assignedTo)
                    )
                }

                // Reload assigned users
                loadAssignedUsers()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isReassigningTask = false,
                        errorMessage = "Failed to reassign task: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadAssignedUsers() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            if (task.assignedTo.isEmpty()) return@launch

            try {
                val users = userRepository.getUsersByIds(task.assignedTo)
                _uiState.update { it.copy(assignedUsers = users) }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }
}

data class GroupTaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val assignedUsers: List<User> = emptyList(),
    val errorMessage: String? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val showEditTaskDialog: Boolean = false,
    val showAssignmentDialog: Boolean = false,
    val isReassigningTask: Boolean = false,
    val groupMembers: List<User> = emptyList(),
)

class GroupTaskDetailViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupTaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupTaskDetailViewModel(taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
