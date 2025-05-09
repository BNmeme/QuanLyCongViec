package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quanlycongviec.ui.theme.TaskPriority1
import com.example.quanlycongviec.ui.theme.TaskPriority2
import com.example.quanlycongviec.ui.theme.TaskPriority3
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupTaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    viewModel: GroupTaskDetailViewModel = viewModel(factory = GroupTaskDetailViewModelFactory(taskId))
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Task", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // Only show edit/delete options to users who can manage tasks
                    if (uiState.canManageTask) {
                        IconButton(onClick = { viewModel.showEditTaskDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Task",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(onClick = { viewModel.showAssignmentDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Reassign Task",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(onClick = { viewModel.showDeleteConfirmationDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
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
        } else if (uiState.task == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Task not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Task title and completion status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.task!!.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Only managers can directly mark tasks as complete
                    if (uiState.canManageTask) {
                        Checkbox(
                            checked = uiState.task!!.isCompleted,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleTaskCompletion(isChecked)
                            }
                        )
                    } else {
                        // For non-managers, just show the completion status
                        Icon(
                            imageVector = if (uiState.task!!.isCompleted)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (uiState.task!!.isCompleted) "Completed" else "Pending",
                            tint = if (uiState.task!!.isCompleted)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task description card
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
                            text = uiState.task!!.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Completion confirmation for assigned members
                if (uiState.isAssignedToTask) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Task Completion",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Confirm when you've completed your part of this task:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val hasConfirmed = uiState.task!!.hasUserConfirmedCompletion(uiState.currentUserId)

                            Button(
                                onClick = { viewModel.confirmTaskCompletion(!hasConfirmed) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasConfirmed)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (hasConfirmed)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (hasConfirmed)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Default.Check,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = if (hasConfirmed)
                                        "I've Completed This Task"
                                    else
                                        "Mark as Completed by Me"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Group information and assigned members
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Group Task",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This task is assigned to members of your group",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )

                        if (uiState.assignedUsers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Assigned to:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Show assigned members with their completion status
                            uiState.assignedUsers.forEach { user ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    // User avatar
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.name.first().toString(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = user.name + if (user.id == uiState.currentUserId) " (You)" else "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Completion status icon
                                    val hasConfirmed = uiState.task!!.hasUserConfirmedCompletion(user.id)

                                    Icon(
                                        imageVector = if (hasConfirmed)
                                            Icons.Default.CheckCircle
                                        else
                                            Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (hasConfirmed) "Completed" else "Pending",
                                        tint = if (hasConfirmed)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Show completion progress
                            Spacer(modifier = Modifier.height(16.dp))

                            val confirmationCount = uiState.task!!.getConfirmationCount()
                            val totalAssigned = uiState.task!!.assignedTo.size

                            LinearProgressIndicator(
                                progress = confirmationCount.toFloat() / totalAssigned.toFloat(),
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "$confirmationCount of $totalAssigned members confirmed completion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task metadata
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
                            text = "Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            val priorityColor = when (uiState.task!!.priority) {
                                1 -> TaskPriority1
                                2 -> TaskPriority2
                                else -> TaskPriority3
                            }

                            val priorityText = when (uiState.task!!.priority) {
                                1 -> "High"
                                2 -> "Medium"
                                else -> "Low"
                            }

                            Text(
                                text = priorityText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = priorityColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Due Date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Text(
                                text = formatDate(uiState.task!!.dueDate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Created",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Text(
                                text = formatDate(uiState.task!!.createdAt),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Text(
                                text = if (uiState.task!!.isCompleted) "Completed" else "Pending",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.task!!.isCompleted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (uiState.showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteConfirmationDialog() },
                title = { Text("Delete Task") },
                text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTask {
                                onNavigateBack()
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

        // Edit task dialog
        if (uiState.showEditTaskDialog) {
            if (uiState.task != null) {
                LaunchedEffect(Unit) {
                    viewModel.hideEditTaskDialog()
                    onNavigateToEditTask(uiState.task!!.id)
                }
            } else {
                AlertDialog(
                    onDismissRequest = { viewModel.hideEditTaskDialog() },
                    title = { Text("Error") },
                    text = { Text("Task not found") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.hideEditTaskDialog() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }

        // Task assignment dialog
        if (uiState.showAssignmentDialog && uiState.task != null) {
            TaskAssignmentDialog(
                task = uiState.task!!,
                groupMembers = uiState.groupMembers,
                onDismiss = { viewModel.hideAssignmentDialog() },
                onAssign = { assignedTo -> viewModel.reassignTask(assignedTo) },
                isLoading = uiState.isReassigningTask
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}
