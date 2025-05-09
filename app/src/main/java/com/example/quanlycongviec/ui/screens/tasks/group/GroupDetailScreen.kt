package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.domain.model.GroupRole
import com.example.quanlycongviec.domain.model.User
import com.example.quanlycongviec.ui.components.TaskItem
import com.example.quanlycongviec.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupDetailViewModel = viewModel(factory = GroupDetailViewModelFactory(groupId))
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.group?.name ?: "Group Details",
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.isCurrentUserCreator) {
                        IconButton(onClick = { viewModel.showEditGroupDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Group",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(onClick = { viewModel.showDeleteConfirmationDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Group",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isCurrentUserCreator) {
                FloatingActionButton(
                    onClick = { viewModel.showAddMemberDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Member",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.group == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Group not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Group description
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = uiState.group?.description ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${uiState.members.size} members",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Members list
                items(uiState.members) { member ->
                    MemberItem(
                        user = member,
                        role = viewModel.getMemberRole(member.id),
                        canRemove = uiState.isCurrentUserCreator && member.id != uiState.currentUserId,
                        canChangeRole = uiState.isCurrentUserCreator && member.id != uiState.currentUserId,
                        onRemove = { viewModel.showRemoveMemberConfirmationDialog(member) },
                        onChangeRole = { viewModel.showChangeRoleDialog(member) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Group Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (uiState.canCurrentUserManageTasks) {
                            TextButton(onClick = { viewModel.showAddTaskDialog() }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Task"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Task")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Add this section after the members items
                if (uiState.isLoadingTasks) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.groupTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks for this group yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    items(uiState.groupTasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = {
                                navController.navigate("${Screen.GroupTaskDetail.route}/${task.id}")
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Edit Group Dialog
        if (uiState.showEditGroupDialog) {
            EditGroupDialog(
                name = uiState.editGroupName,
                description = uiState.editGroupDescription,
                onNameChange = { viewModel.updateEditGroupName(it) },
                onDescriptionChange = { viewModel.updateEditGroupDescription(it) },
                onDismiss = { viewModel.hideEditGroupDialog() },
                onSave = { viewModel.updateGroup() },
                isLoading = uiState.isUpdating,
                nameError = uiState.editGroupNameError,
                descriptionError = uiState.editGroupDescriptionError
            )
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteConfirmationDialog() },
                title = { Text("Delete Group") },
                text = { Text("Are you sure you want to delete this group? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteGroup {
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteConfirmationDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Member Dialog
        if (uiState.showAddMemberDialog) {
            AddMemberDialog(
                email = uiState.newMemberEmail,
                onEmailChange = { viewModel.updateNewMemberEmail(it) },
                onDismiss = { viewModel.hideAddMemberDialog() },
                onAdd = { viewModel.addMember() },
                isLoading = uiState.isAddingMember,
                emailError = uiState.newMemberEmailError,
                errorMessage = uiState.addMemberErrorMessage
            )
        }

        // Remove Member Confirmation Dialog
        if (uiState.showRemoveMemberConfirmationDialog && uiState.memberToRemove != null) {
            AlertDialog(
                onDismissRequest = { viewModel.hideRemoveMemberConfirmationDialog() },
                title = { Text("Remove Member") },
                text = { Text("Are you sure you want to remove ${uiState.memberToRemove?.name} from this group?") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.removeMember() }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideRemoveMemberConfirmationDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Change Role Dialog
        if (uiState.showChangeRoleDialog && uiState.memberToChangeRole != null) {
            ChangeRoleDialog(
                member = uiState.memberToChangeRole!!,
                selectedRole = uiState.selectedRole ?: GroupRole.MEMBER,
                onRoleSelected = { viewModel.updateSelectedRole(it) },
                onDismiss = { viewModel.hideChangeRoleDialog() },
                onConfirm = { viewModel.changeRole() },
                isLoading = uiState.isChangingRole
            )
        }

        if (uiState.showAddTaskDialog) {
            AddGroupTaskDialog(
                onDismiss = { viewModel.hideAddTaskDialog() },
                onTaskAdded = { task ->
                    viewModel.addTask(task) {}
                },
                currentUserId = uiState.currentUserId,
                groups = listOf(uiState.group!!),
                groupMembers = mapOf(uiState.group!!.id to uiState.members),
                isLoading = uiState.isAddingTask
            )
        }
    }
}

@Composable
fun MemberItem(
    user: User,
    role: GroupRole,
    canRemove: Boolean,
    canChangeRole: Boolean,
    onRemove: () -> Unit,
    onChangeRole: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.first().toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Role badge
            Card(
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (role) {
                        GroupRole.LEADER -> MaterialTheme.colorScheme.primaryContainer
                        GroupRole.DEPUTY -> MaterialTheme.colorScheme.secondaryContainer
                        GroupRole.MEMBER -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = when (role) {
                        GroupRole.LEADER -> "Leader"
                        GroupRole.DEPUTY -> "Deputy"
                        GroupRole.MEMBER -> "Member"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (role) {
                        GroupRole.LEADER -> MaterialTheme.colorScheme.onPrimaryContainer
                        GroupRole.DEPUTY -> MaterialTheme.colorScheme.onSecondaryContainer
                        GroupRole.MEMBER -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (canChangeRole || canRemove) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Member Options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (canChangeRole) {
                            DropdownMenuItem(
                                text = { Text("Change Role") },
                                onClick = {
                                    showMenu = false
                                    onChangeRole()
                                }
                            )
                        }

                        if (canRemove) {
                            DropdownMenuItem(
                                text = { Text("Remove from Group") },
                                onClick = {
                                    showMenu = false
                                    onRemove()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChangeRoleDialog(
    member: User,
    selectedRole: GroupRole,
    onRoleSelected: (GroupRole) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Role for ${member.name}") },
        text = {
            Column {
                Text(
                    text = "Select a role for this member:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Cannot change to LEADER as that's reserved for the creator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedRole == GroupRole.DEPUTY,
                        onClick = { onRoleSelected(GroupRole.DEPUTY) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Deputy",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Can manage tasks and assign them to members",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedRole == GroupRole.MEMBER,
                        onClick = { onRoleSelected(GroupRole.MEMBER) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Member",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Can view and complete assigned tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditGroupDialog(
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isLoading: Boolean,
    nameError: String?,
    descriptionError: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null
                )

                if (nameError != null) {
                    Text(
                        text = nameError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = descriptionError != null,
                    minLines = 3,
                    maxLines = 5
                )

                if (descriptionError != null) {
                    Text(
                        text = descriptionError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddMemberDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    isLoading: Boolean,
    emailError: String?,
    errorMessage: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Member") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError != null
                )

                if (emailError != null) {
                    Text(
                        text = emailError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAdd,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
