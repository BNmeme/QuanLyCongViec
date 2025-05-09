package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Group
import com.example.quanlycongviec.domain.model.GroupRole
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
    private val authRepository = AppModule.provideAuthRepository()
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
                val currentUserId = authRepository.getCurrentUserId() ?: ""
                val task = taskRepository.getTaskById(taskId)

                // Load assigned users
                val assignedUsers = if (task.assignedTo.isNotEmpty()) {
                    userRepository.getUsersByIds(task.assignedTo)
                } else {
                    emptyList()
                }

                // Load group to check permissions
                val group = if (task.isGroupTask && task.groupId.isNotEmpty()) {
                    groupRepository.getGroupById(task.groupId)
                } else {
                    null
                }

                val canManageTask = group?.canManageTasks(currentUserId) ?: false
                val isAssignedToTask = task.assignedTo.contains(currentUserId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        task = task,
                        assignedUsers = assignedUsers,
                        currentUserId = currentUserId,
                        canManageTask = canManageTask,
                        isAssignedToTask = isAssignedToTask,
                        group = group
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
                // Only managers can toggle task completion
                if (!_uiState.value.canManageTask) return@launch

                taskRepository.toggleTaskCompletion(taskId, isCompleted)

                // If marking as incomplete, reset all completion confirmations
                if (!isCompleted) {
                    taskRepository.resetAllCompletionConfirmations(taskId)
                }

                // Update local state
                _uiState.update {
                    val updatedTask = it.task?.copy(
                        isCompleted = isCompleted,
                        completionConfirmations = if (!isCompleted)
                            it.task.assignedTo.associateWith { false }
                        else
                            it.task.completionConfirmations
                    )

                    it.copy(task = updatedTask)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update task: ${e.message}")
                }
            }
        }
    }

    fun confirmTaskCompletion(isConfirmed: Boolean) {
        viewModelScope.launch {
            try {
                val currentUserId = _uiState.value.currentUserId

                // Only assigned members can confirm completion
                if (!_uiState.value.isAssignedToTask) return@launch

                taskRepository.confirmTaskCompletion(taskId, currentUserId, isConfirmed)

                // Update local state
                _uiState.update {
                    val updatedConfirmations = it.task?.completionConfirmations?.toMutableMap() ?: mutableMapOf()
                    updatedConfirmations[currentUserId] = isConfirmed

                    val updatedTask = it.task?.copy(
                        completionConfirmations = updatedConfirmations
                    )

                    it.copy(task = updatedTask)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to confirm task completion: ${e.message}")
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
                // Only managers can delete tasks
                if (!_uiState.value.canManageTask) return@launch

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
                // Only managers can assign tasks
                if (!_uiState.value.canManageTask) return@launch

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
            // Only managers can reassign tasks
            if (!_uiState.value.canManageTask) return@launch

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
    val currentUserId: String = "",
    val canManageTask: Boolean = false,
    val isAssignedToTask: Boolean = false,
    val group: Group? = null
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
