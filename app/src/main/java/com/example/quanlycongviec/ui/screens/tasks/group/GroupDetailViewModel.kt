package com.example.quanlycongviec.ui.screens.tasks.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.data.repository.NotificationRepository
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

class GroupDetailViewModel(
    private val groupId: String
) : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val groupRepository = AppModule.provideGroupRepository()
    private val userRepository = AppModule.provideUserRepository()
    private val taskRepository = AppModule.provideTaskRepository()
    private val notificationRepository = AppModule.provideNotificationRepository()

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroupDetails()
        loadGroupTasks()
    }

    private fun loadGroupDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentUserId = authRepository.getCurrentUserId() ?: ""
                val group = groupRepository.getGroupById(groupId)
                val members = userRepository.getUsersByIds(group.members)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        group = group,
                        members = members,
                        currentUserId = currentUserId,
                        isCurrentUserCreator = group.createdBy == currentUserId,
                        canCurrentUserManageTasks = group.canManageTasks(currentUserId),
                        editGroupName = group.name,
                        editGroupDescription = group.description
                    )
                }

                // If we already have tasks loaded, recalculate stats
                if (_uiState.value.groupTasks.isNotEmpty()) {
                    calculateMemberTaskStats()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load group details: ${e.message}"
                    )
                }
            }
        }
    }

    // Edit Group Dialog
    fun showEditGroupDialog() {
        _uiState.update { it.copy(showEditGroupDialog = true) }
    }

    fun hideEditGroupDialog() {
        _uiState.update { it.copy(showEditGroupDialog = false) }
    }

    fun updateEditGroupName(name: String) {
        _uiState.update { it.copy(editGroupName = name, editGroupNameError = null) }
    }

    fun updateEditGroupDescription(description: String) {
        _uiState.update { it.copy(editGroupDescription = description, editGroupDescriptionError = null) }
    }

    fun updateGroup() {
        if (!validateEditGroupInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }

            try {
                val updatedGroup = _uiState.value.group?.copy(
                    name = _uiState.value.editGroupName,
                    description = _uiState.value.editGroupDescription
                )

                if (updatedGroup != null) {
                    groupRepository.updateGroup(updatedGroup)
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            group = updatedGroup,
                            showEditGroupDialog = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        errorMessage = "Failed to update group: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateEditGroupInputs(): Boolean {
        var isValid = true

        if (_uiState.value.editGroupName.isBlank()) {
            _uiState.update { it.copy(editGroupNameError = "Group name cannot be empty") }
            isValid = false
        }

        if (_uiState.value.editGroupDescription.isBlank()) {
            _uiState.update { it.copy(editGroupDescriptionError = "Description cannot be empty") }
            isValid = false
        }

        return isValid
    }

    // Delete Group
    fun showDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = true) }
    }

    fun hideDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    fun deleteGroup(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // First delete all tasks associated with this group
                taskRepository.deleteTasksByGroupId(groupId)

                // Then delete the group itself
                groupRepository.deleteGroup(groupId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to delete group: ${e.message}",
                        showDeleteConfirmationDialog = false
                    )
                }
            }
        }
    }

    // Add Member
    fun showAddMemberDialog() {
        _uiState.update { it.copy(showAddMemberDialog = true) }
    }

    fun hideAddMemberDialog() {
        _uiState.update {
            it.copy(
                showAddMemberDialog = false,
                newMemberEmail = "",
                newMemberEmailError = null,
                addMemberErrorMessage = null
            )
        }
    }

    fun updateNewMemberEmail(email: String) {
        _uiState.update { it.copy(newMemberEmail = email, newMemberEmailError = null) }
    }

    fun addMember() {
        if (!validateAddMemberInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingMember = true, addMemberErrorMessage = null) }

            try {
                // Find user by email
                val email = _uiState.value.newMemberEmail
                Log.d("GroupDetailViewModel", "Looking for user with email: $email")
                val userByEmail = userRepository.getUserByEmail(email)

                if (userByEmail == null) {
                    Log.e("GroupDetailViewModel", "User with email $email not found")
                    _uiState.update {
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "User with this email not found"
                        )
                    }
                    return@launch
                }

                Log.d("GroupDetailViewModel", "Found user: ${userByEmail.id}, ${userByEmail.name}, ${userByEmail.email}")

                // Check if user is already a member
                if (_uiState.value.group?.members?.contains(userByEmail.id) == true) {
                    Log.e("GroupDetailViewModel", "User is already a member of this group")
                    _uiState.update {
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "User is already a member of this group"
                        )
                    }
                    return@launch
                }

                // Check if user already has a pending invitation
                if (_uiState.value.group?.pendingInvitations?.contains(userByEmail.id) == true) {
                    Log.e("GroupDetailViewModel", "User already has a pending invitation")
                    _uiState.update {
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "User already has a pending invitation"
                        )
                    }
                    return@launch
                }

                // Get current user
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                val currentUser = userRepository.getUserById(currentUserId)
                if (currentUser == null) {
                    Log.e("GroupDetailViewModel", "Current user not found")
                    _uiState.update {
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "Failed to get current user"
                        )
                    }
                    return@launch
                }

                // Get group
                val group = _uiState.value.group
                if (group == null) {
                    Log.e("GroupDetailViewModel", "Group is null")
                    _uiState.update {
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "Failed to get group"
                        )
                    }
                    return@launch
                }

                // Add member to pending invitations
                Log.d("GroupDetailViewModel", "Adding user ${userByEmail.id} to pending invitations for group $groupId")
                groupRepository.addMemberToGroup(groupId, userByEmail.id)

                // Create notification
                try {
                    Log.d("GroupDetailViewModel", "Creating notification for user ${userByEmail.id}")
                    val notificationId = notificationRepository.createGroupInvitationNotification(
                        userId = userByEmail.id,
                        groupName = group.name,
                        groupId = group.id,
                        invitedByName = currentUser.name ?: "Someone"
                    )
                    Log.d("GroupDetailViewModel", "Created notification with ID: $notificationId")
                } catch (e: Exception) {
                    Log.e("GroupDetailViewModel", "Error creating notification: ${e.message}", e)
                    // Continue even if notification creation fails
                }

                // Reload group details
                loadGroupDetails()

                _uiState.update {
                    it.copy(
                        isAddingMember = false,
                        showAddMemberDialog = false,
                        newMemberEmail = ""
                    )
                }

                Log.d("GroupDetailViewModel", "Successfully added member to group")
            } catch (e: Exception) {
                Log.e("GroupDetailViewModel", "Error adding member: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isAddingMember = false,
                        addMemberErrorMessage = "Failed to add member: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateAddMemberInputs(): Boolean {
        var isValid = true

        if (_uiState.value.newMemberEmail.isBlank()) {
            _uiState.update { it.copy(newMemberEmailError = "Email cannot be empty") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.newMemberEmail).matches()) {
            _uiState.update { it.copy(newMemberEmailError = "Please enter a valid email address") }
            isValid = false
        }

        return isValid
    }

    // Remove Member
    fun showRemoveMemberConfirmationDialog(member: User) {
        _uiState.update {
            it.copy(
                showRemoveMemberConfirmationDialog = true,
                memberToRemove = member
            )
        }
    }

    fun hideRemoveMemberConfirmationDialog() {
        _uiState.update {
            it.copy(
                showRemoveMemberConfirmationDialog = false,
                memberToRemove = null
            )
        }
    }

    fun removeMember() {
        val memberToRemove = _uiState.value.memberToRemove ?: return

        viewModelScope.launch {
            try {
                groupRepository.removeMemberFromGroup(groupId, memberToRemove.id)

                // Reload group details
                loadGroupDetails()

                _uiState.update {
                    it.copy(
                        showRemoveMemberConfirmationDialog = false,
                        memberToRemove = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to remove member: ${e.message}",
                        showRemoveMemberConfirmationDialog = false
                    )
                }
            }
        }
    }

    fun loadGroupTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTasks = true) }

            try {
                val tasks = taskRepository.getTasksByGroupId(groupId)
                _uiState.update {
                    it.copy(
                        isLoadingTasks = false,
                        groupTasks = tasks
                    )
                }
                calculateMemberTaskStats(tasks)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingTasks = false,
                        errorMessage = "Failed to load tasks: ${e.message}"
                    )
                }
            }
        }
    }

    fun showAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = true) }
    }

    fun hideAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = false) }
    }

    fun addTask(task: Task, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingTask = true) }

            try {
                taskRepository.createTask(task)
                loadGroupTasks() // This will now properly recalculate stats
                _uiState.update { it.copy(isAddingTask = false, showAddTaskDialog = false) }
                onComplete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingTask = false,
                        errorMessage = "Failed to add task: ${e.message}"
                    )
                }
            }
        }
    }

    // Role Management
    fun showChangeRoleDialog(member: User) {
        val group = _uiState.value.group ?: return
        val currentRole = group.getMemberRole(member.id)

        _uiState.update {
            it.copy(
                showChangeRoleDialog = true,
                memberToChangeRole = member,
                selectedRole = currentRole
            )
        }
    }

    fun hideChangeRoleDialog() {
        _uiState.update {
            it.copy(
                showChangeRoleDialog = false,
                memberToChangeRole = null,
                selectedRole = null
            )
        }
    }

    fun updateSelectedRole(role: GroupRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun changeRole() {
        val member = _uiState.value.memberToChangeRole ?: return
        val role = _uiState.value.selectedRole ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingRole = true) }

            try {
                groupRepository.updateMemberRole(groupId, member.id, role)

                // Reload group details
                loadGroupDetails()

                _uiState.update {
                    it.copy(
                        isChangingRole = false,
                        showChangeRoleDialog = false,
                        memberToChangeRole = null,
                        selectedRole = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChangingRole = false,
                        errorMessage = "Failed to change role: ${e.message}"
                    )
                }
            }
        }
    }

    fun getMemberRole(userId: String): GroupRole {
        return _uiState.value.group?.getMemberRole(userId) ?: GroupRole.MEMBER
    }

    private suspend fun getCurrentUser(): User? {
        val currentUserId = authRepository.getCurrentUserId() ?: return null
        return userRepository.getUserById(currentUserId)
    }

    private fun loadGroupData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentUserId = authRepository.getCurrentUserId() ?: ""
                val group = groupRepository.getGroupById(groupId)
                val members = userRepository.getUsersByIds(group.members)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        group = group,
                        members = members,
                        currentUserId = currentUserId,
                        isCurrentUserCreator = group.createdBy == currentUserId,
                        canCurrentUserManageTasks = group.canManageTasks(currentUserId),
                        editGroupName = group.name,
                        editGroupDescription = group.description
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load group details: ${e.message}"
                    )
                }
            }
        }
    }

    fun inviteMember(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingMember = true, addMemberErrorMessage = null) }

            try {
                // Find user by email
                val user = userRepository.getUserByEmail(email)
                if (user != null) {
                    // Check if user is already a member
                    if (_uiState.value.group?.members?.contains(user.id) == true) {
                        _uiState.update {
                            it.copy(
                                isAddingMember = false,
                                addMemberErrorMessage = "User is already a member of this group"
                            )
                        }
                        return@launch
                    }

                    // Check if user already has a pending invitation
                    if (_uiState.value.group?.pendingInvitations?.contains(user.id) == true) {
                        _uiState.update {
                            it.copy(
                                isAddingMember = false,
                                addMemberErrorMessage = "User already has a pending invitation"
                            )
                        }
                        return@launch
                    }

                    // Add invitation
                    val groupId = _uiState.value.group?.id ?: return@launch
                    groupRepository.addMemberToGroup(groupId, user.id)

                    // Send notification
                    val currentUser = getCurrentUser() ?: return@launch
                    val group = _uiState.value.group ?: return@launch

                    notificationRepository.createGroupInvitationNotification(
                        userId = user.id,
                        groupName = group.name,
                        groupId = group.id,
                        invitedByName = currentUser.name ?: "Someone"
                    )

                    // Refresh group data
                    loadGroupData()

                    _uiState.update {
                        it.copy(
                            isAddingMember = false,
                            showAddMemberDialog = false,
                            newMemberEmail = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "User not found with this email"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingMember = false,
                        addMemberErrorMessage = "Failed to add member: ${e.message}"
                    )
                }
            }
        }
    }

    private fun calculateMemberTaskStats(tasks: List<Task> = _uiState.value.groupTasks) {
        viewModelScope.launch {
            val members = _uiState.value.members
            val memberStats = mutableMapOf<String, MemberTaskStats>()

            // Initialize stats for all members
            members.forEach { member ->
                memberStats[member.id] = MemberTaskStats(
                    userId = member.id,
                    userName = member.name
                )
            }

            // Calculate stats based on tasks
            tasks.forEach { task ->
                task.assignedTo.forEach { userId ->
                    val currentStats = memberStats[userId] ?: return@forEach
                    val newTotalAssigned = currentStats.totalAssigned + 1
                    val newCompleted = if (task.hasUserConfirmedCompletion(userId)) {
                        currentStats.completed + 1
                    } else {
                        currentStats.completed
                    }
                    val newCompletionRate = if (newTotalAssigned > 0) {
                        newCompleted.toFloat() / newTotalAssigned
                    } else {
                        0f
                    }

                    memberStats[userId] = currentStats.copy(
                        totalAssigned = newTotalAssigned,
                        completed = newCompleted,
                        completionRate = newCompletionRate
                    )
                }
            }

            _uiState.update { it.copy(memberTaskStats = memberStats) }
        }
    }
}

data class GroupDetailUiState(
    val isLoading: Boolean = false,
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val currentUserId: String = "",
    val isCurrentUserCreator: Boolean = false,
    val canCurrentUserManageTasks: Boolean = false,
    val errorMessage: String? = null,

    // Edit Group
    val showEditGroupDialog: Boolean = false,
    val editGroupName: String = "",
    val editGroupDescription: String = "",
    val editGroupNameError: String? = null,
    val editGroupDescriptionError: String? = null,
    val isUpdating: Boolean = false,

    // Delete Group
    val showDeleteConfirmationDialog: Boolean = false,

    // Add Member
    val showAddMemberDialog: Boolean = false,
    val newMemberEmail: String = "",
    val newMemberEmailError: String? = null,
    val isAddingMember: Boolean = false,
    val addMemberErrorMessage: String? = null,

    // Remove Member
    val showRemoveMemberConfirmationDialog: Boolean = false,
    val memberToRemove: User? = null,

    // Group Tasks
    val groupTasks: List<Task> = emptyList(),
    val isLoadingTasks: Boolean = false,
    val showAddTaskDialog: Boolean = false,
    val isAddingTask: Boolean = false,

    // Role Management
    val showChangeRoleDialog: Boolean = false,
    val memberToChangeRole: User? = null,
    val selectedRole: GroupRole? = null,
    val isChangingRole: Boolean = false,

    // Member Statistics
    val memberTaskStats: Map<String, MemberTaskStats> = emptyMap()
)

data class MemberTaskStats(
    val userId: String = "",
    val userName: String = "",
    val totalAssigned: Int = 0,
    val completed: Int = 0,
    val completionRate: Float = 0f
)

class GroupDetailViewModelFactory(private val groupId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupDetailViewModel(groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
