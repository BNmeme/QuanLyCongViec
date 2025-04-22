package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.domain.model.User

@Composable
fun TaskAssignmentDialog(
    task: Task,
    groupMembers: List<User>,
    onDismiss: () -> Unit,
    onAssign: (List<String>) -> Unit,
    isLoading: Boolean
) {
    var selectedUserIds by remember { mutableStateOf(task.assignedTo ?: emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Task") },
        text = {
            Column {
                Text("Select members to assign this task to:")

                Spacer(modifier = Modifier.height(8.dp))

                if (groupMembers.isEmpty()) {
                    Text(
                        "No group members available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(groupMembers) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedUserIds.contains(user.id),
                                    onCheckedChange = { isChecked ->
                                        selectedUserIds = if (isChecked) {
                                            selectedUserIds + user.id
                                        } else {
                                            selectedUserIds - user.id
                                        }
                                    }
                                )

                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(selectedUserIds) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Assign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
