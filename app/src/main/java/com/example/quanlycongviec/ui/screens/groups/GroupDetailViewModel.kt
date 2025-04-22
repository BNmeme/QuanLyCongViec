package com.example.quanlycongviec.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Group
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
    
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadGroupDetails()
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
                val userByEmail = userRepository.getUserByEmail(_uiState.value.newMemberEmail)
                
                if (userByEmail == null) {
                    _uiState.update { 
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "User with this email not found"
                        ) 
                    }
                    return@launch
                }
                
                // Check if user is already a member
                if (_uiState.value.group?.members?.contains(userByEmail.id) == true) {
                    _uiState.update { 
                        it.copy(
                            isAddingMember = false,
                            addMemberErrorMessage = "User is already a member of this group"
                        ) 
                    }
                    return@launch
                }
                
                // Add member to group
                groupRepository.addMemberToGroup(groupId, userByEmail.id)
                
                // Reload group details
                loadGroupDetails()
                
                _uiState.update { 
                    it.copy(
                        isAddingMember = false,
                        showAddMemberDialog = false,
                        newMemberEmail = ""
                    ) 
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
}

data class GroupDetailUiState(
    val isLoading: Boolean = false,
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val currentUserId: String = "",
    val isCurrentUserCreator: Boolean = false,
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
    val memberToRemove: User? = null
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
