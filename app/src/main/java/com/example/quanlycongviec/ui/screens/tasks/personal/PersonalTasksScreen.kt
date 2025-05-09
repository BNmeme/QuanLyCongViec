package com.example.quanlycongviec.ui.screens.tasks.personal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.domain.model.Label
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.ui.navigation.Screen
import com.example.quanlycongviec.ui.theme.TaskPriority1
import com.example.quanlycongviec.ui.theme.TaskPriority2
import com.example.quanlycongviec.ui.theme.TaskPriority3
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonalTasksScreen(
    navController: NavController,
    viewModel: PersonalTasksViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Tasks", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Label filter button
                    IconButton(onClick = { viewModel.toggleLabelFilterMenu() }) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "Filter by Label",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Status filter button
                    IconButton(onClick = { viewModel.toggleFilterMenu() }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Filter Tasks",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Error message
                uiState.errorMessage?.let { error ->
                    androidx.compose.material3.Snackbar(
                        modifier = Modifier.padding(vertical = 8.dp),
                        action = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }

                // Filter panels
                AnimatedVisibility(
                    visible = uiState.isFilterMenuOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    FilterPanel(
                        activeFilter = uiState.activeFilter,
                        onFilterSelected = { viewModel.setFilter(it) },
                        onClose = { viewModel.closeFilterMenus() }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.isLabelFilterMenuOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    LabelFilterPanel(
                        labels = uiState.availableLabels,
                        selectedLabelId = uiState.selectedLabelId,
                        onLabelSelected = { viewModel.setLabelFilter(it) },
                        onClose = { viewModel.closeFilterMenus() }
                    )
                }

                // Active filters display
                if (uiState.activeFilter != TaskFilter.ALL || uiState.selectedLabelId != null) {
                    ActiveFiltersBar(
                        activeFilter = uiState.activeFilter,
                        selectedLabel = uiState.availableLabels.find { it.id == uiState.selectedLabelId },
                        onClearFilter = { viewModel.clearAllFilters() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (uiState.tasks.isEmpty())
                                    "No tasks found. Create your first task!"
                                else
                                    "No tasks match the current filters.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            if (uiState.activeFilter != TaskFilter.ALL || uiState.selectedLabelId != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.clearAllFilters() }
                                ) {
                                    Text("Clear Filters")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredTasks) { task ->
                            TaskCard(
                                task = task,
                                labels = uiState.availableLabels.filter { task.labels.contains(it.id) },
                                onClick = {
                                    navController.navigate("${Screen.PersonalTaskDetail.route}/${task.id}")
                                },
                                onCheckChange = { isCompleted ->
                                    viewModel.toggleTaskCompletion(task.id, isCompleted)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                        }
                    }
                }
            }
        }

        if (showAddTaskDialog) {
            AddPersonalTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onTaskAdded = { task ->
                    viewModel.addTask(task) {
                        showAddTaskDialog = false
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterPanel(
    activeFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    onClose: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeFilter == TaskFilter.ALL,
                    onClick = { onFilterSelected(TaskFilter.ALL) },
                    label = { Text("All") },
                    leadingIcon = if (activeFilter == TaskFilter.ALL) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == TaskFilter.PENDING,
                    onClick = { onFilterSelected(TaskFilter.PENDING) },
                    label = { Text("Pending") },
                    leadingIcon = if (activeFilter == TaskFilter.PENDING) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == TaskFilter.COMPLETED,
                    onClick = { onFilterSelected(TaskFilter.COMPLETED) },
                    label = { Text("Completed") },
                    leadingIcon = if (activeFilter == TaskFilter.COMPLETED) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == TaskFilter.HIGH_PRIORITY,
                    onClick = { onFilterSelected(TaskFilter.HIGH_PRIORITY) },
                    label = { Text("High Priority") },
                    leadingIcon = if (activeFilter == TaskFilter.HIGH_PRIORITY) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == TaskFilter.MEDIUM_PRIORITY,
                    onClick = { onFilterSelected(TaskFilter.MEDIUM_PRIORITY) },
                    label = { Text("Medium Priority") },
                    leadingIcon = if (activeFilter == TaskFilter.MEDIUM_PRIORITY) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == TaskFilter.LOW_PRIORITY,
                    onClick = { onFilterSelected(TaskFilter.LOW_PRIORITY) },
                    label = { Text("Low Priority") },
                    leadingIcon = if (activeFilter == TaskFilter.LOW_PRIORITY) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == TaskFilter.DUE_TODAY,
                    onClick = { onFilterSelected(TaskFilter.DUE_TODAY) },
                    label = { Text("Due Today") },
                    leadingIcon = if (activeFilter == TaskFilter.DUE_TODAY) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == TaskFilter.DUE_THIS_WEEK,
                    onClick = { onFilterSelected(TaskFilter.DUE_THIS_WEEK) },
                    label = { Text("Due This Week") },
                    leadingIcon = if (activeFilter == TaskFilter.DUE_THIS_WEEK) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelFilterPanel(
    labels: List<Label>,
    selectedLabelId: String?,
    onLabelSelected: (String?) -> Unit,
    onClose: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Label",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (labels.isEmpty()) {
                Text(
                    text = "No labels available. Create labels in the settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clear label filter option
                    FilterChip(
                        selected = selectedLabelId == null,
                        onClick = { onLabelSelected(null) },
                        label = { Text("All Labels") },
                        leadingIcon = if (selectedLabelId == null) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    // Label filter options
                    labels.forEach { label ->
                        FilterChip(
                            selected = selectedLabelId == label.id,
                            onClick = { onLabelSelected(label.id) },
                            label = { Text(label.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(label.getColorValue(), CircleShape)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFiltersBar(
    activeFilter: TaskFilter,
    selectedLabel: Label?,
    onClearFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Active Filters:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 8.dp)
        )

        if (activeFilter != TaskFilter.ALL) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getFilterDisplayName(activeFilter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (selectedLabel != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(selectedLabel.getColorValue(), CircleShape)
                            .padding(end = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = selectedLabel.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        IconButton(
            onClick = onClearFilter,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear Filters",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Helper function to get display name for filter
@Composable
fun getFilterDisplayName(filter: TaskFilter): String {
    return when (filter) {
        TaskFilter.ALL -> "All"
        TaskFilter.COMPLETED -> "Completed"
        TaskFilter.PENDING -> "Pending"
        TaskFilter.HIGH_PRIORITY -> "High Priority"
        TaskFilter.MEDIUM_PRIORITY -> "Medium Priority"
        TaskFilter.LOW_PRIORITY -> "Low Priority"
        TaskFilter.DUE_TODAY -> "Due Today"
        TaskFilter.DUE_THIS_WEEK -> "Due This Week"
        TaskFilter.BY_LABEL -> "By Label"
    }
}

@Composable
fun TaskCard(
    task: Task,
    labels: List<Label>,
    onClick: () -> Unit,
    onCheckChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority indicator
                val priorityColor = when (task.priority) {
                    1 -> TaskPriority1
                    2 -> TaskPriority2
                    else -> TaskPriority3
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(priorityColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Task details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatDueDate(task.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isTaskOverdue(task.dueDate) && !task.isCompleted)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Checkbox
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCheckChange
                )
            }

            // Show labels if any
            if (labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    labels.take(3).forEach { label ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = label.getColorValue().copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(label.getColorValue(), CircleShape)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = label.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (labels.size > 3) {
                        Text(
                            text = "+${labels.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDueDate(timestamp: Long): String {
    val date = Date(timestamp)
    val today = Calendar.getInstance()
    val dueDate = Calendar.getInstance().apply { time = date }

    return when {
        isSameDay(today, dueDate) -> "Today, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        isYesterday(today, dueDate) -> "Yesterday, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        isTomorrow(today, dueDate) -> "Tomorrow, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(today: Calendar, otherDate: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, otherDate)
}

private fun isTomorrow(today: Calendar, otherDate: Calendar): Boolean {
    val tomorrow = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, 1)
    }
    return isSameDay(tomorrow, otherDate)
}

private fun isTaskOverdue(dueDate: Long): Boolean {
    return dueDate < System.currentTimeMillis()
}
