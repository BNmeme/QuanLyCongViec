package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.quanlycongviec.domain.model.User

@Composable
fun GroupTaskAssignmentDialog(
    members: List<User>,
    currentlyAssigned: List<String>,
    isLoading: Boolean,
    onAssign: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedMembers = remember { mutableStateListOf<String>() }
    
    // Initialize with currently assigned members
    LaunchedEffect(members, currentlyAssigned) {
        selectedMembers.clear()
        selectedMembers.addAll(currentlyAssigned)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Assign Task",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Select members to assign this task to:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (members.isEmpty()) {
                    Text(
                        text = "No members available in this group",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(members) { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedMembers.contains(member.id),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedMembers.add(member.id)
                                        } else {
                                            selectedMembers.remove(member.id)
                                        }
                                    }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = member.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onAssign(selectedMembers.toList()) },
                        enabled = !isLoading && members.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Assign")
                        }
                    }
                }
            }
        }
    }
}
