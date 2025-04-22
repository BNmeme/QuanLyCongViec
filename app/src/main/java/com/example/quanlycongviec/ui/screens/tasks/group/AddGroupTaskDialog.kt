package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.quanlycongviec.domain.model.Group
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.domain.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (Task) -> Unit,
    currentUserId: String,
    groups: List<Group>,
    groupMembers: Map<String, List<User>>,
    isLoading: Boolean = false
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(3) } // Default: Low priority
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000) } // Tomorrow
    var selectedMembers by remember { mutableStateOf<List<String>>(listOf()) }
    
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var groupError by remember { mutableStateOf<String?>(null) }
    
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedGroup by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Group Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        titleError = null
                    },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError != null
                )
                
                if (titleError != null) {
                    Text(
                        text = titleError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        descriptionError = null
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    isError = descriptionError != null
                )
                
                if (descriptionError != null) {
                    Text(
                        text = descriptionError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Priority dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = when (priority) {
                            1 -> "High"
                            2 -> "Medium"
                            else -> "Low"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("High") },
                            onClick = {
                                priority = 1
                                expandedPriority = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Medium") },
                            onClick = {
                                priority = 2
                                expandedPriority = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Low") },
                            onClick = {
                                priority = 3
                                expandedPriority = false
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Group selection
                ExposedDropdownMenuBox(
                    expanded = expandedGroup,
                    onExpandedChange = { expandedGroup = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedGroupId?.let { groupId ->
                            groups.find { it.id == groupId }?.name
                        } ?: "Select Group",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = groupError != null
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedGroup,
                        onDismissRequest = { expandedGroup = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    groupError = null
                                    expandedGroup = false
                                    // Reset selected members when group changes
                                    selectedMembers = listOf(currentUserId)
                                }
                            )
                        }
                    }
                }
                
                if (groupError != null) {
                    Text(
                        text = groupError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Due date selection
                OutlinedTextField(
                    value = formatDate(dueDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Due Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Change")
                        }
                    }
                )
                
                // Member selection (if group is selected)
                selectedGroupId?.let { groupId ->
                    val members = groupMembers[groupId] ?: emptyList()
                    
                    if (members.isNotEmpty()) {
                        Text(
                            text = "Assign to Members:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        members.forEach { member ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedMembers.contains(member.id),
                                    onCheckedChange = { isChecked ->
                                        selectedMembers = if (isChecked) {
                                            selectedMembers + member.id
                                        } else {
                                            selectedMembers - member.id
                                        }
                                    }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                if (member.id == currentUserId) {
                                    Text(
                                        text = " (You)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            // Validate inputs
                            var isValid = true
                            
                            if (title.isBlank()) {
                                titleError = "Title cannot be empty"
                                isValid = false
                            }
                            
                            if (description.isBlank()) {
                                descriptionError = "Description cannot be empty"
                                isValid = false
                            }
                            
                            if (selectedGroupId == null) {
                                groupError = "Please select a group"
                                isValid = false
                            }
                            
                            if (isValid) {
                                val task = Task(
                                    id = UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    isCompleted = false,
                                    createdAt = System.currentTimeMillis(),
                                    dueDate = dueDate,
                                    priority = priority,
                                    userId = currentUserId,
                                    isGroupTask = true,
                                    groupId = selectedGroupId!!,
                                    assignedTo = selectedMembers
                                )
                                
                                onTaskAdded(task)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Add Task")
                        }
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}
