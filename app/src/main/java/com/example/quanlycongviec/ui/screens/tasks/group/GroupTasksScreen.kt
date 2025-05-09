package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.domain.model.Group
import com.example.quanlycongviec.domain.model.GroupRole
import com.example.quanlycongviec.ui.components.TaskItem
import com.example.quanlycongviec.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroupTasksScreen(
    navController: NavController,
    viewModel: GroupTasksViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Tasks", color = MaterialTheme.colorScheme.onPrimary) },
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
                    IconButton(onClick = { viewModel.toggleFilterMenu() }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Filter Tasks",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    IconButton(onClick = { navController.navigate(Screen.Groups.route) }) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Manage Groups",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB if user has permission to add tasks in at least one group
            if (uiState.groupsWithManagePermission.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.showAddTaskDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
                            TextButton(onClick = { /* Clear error */ }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }

                // Filter panel
                AnimatedVisibility(
                    visible = uiState.isFilterMenuOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    GroupFilterPanel(
                        activeFilter = uiState.selectedGroupFilter,
                        onFilterSelected = { viewModel.setGroupTypeFilter(it) },
                        onClose = { viewModel.closeFilterMenu() }
                    )
                }

                // Active filter display
                if (uiState.selectedGroupFilter != GroupFilter.ALL_GROUPS || uiState.selectedGroupId != null) {
                    ActiveGroupFilterBar(
                        groupFilter = uiState.selectedGroupFilter,
                        selectedGroup = uiState.groups.find { it.id == uiState.selectedGroupId },
                        onClearFilter = { viewModel.clearAllFilters() }
                    )
                }

                // Group chips for specific group filtering
                if (uiState.groups.isNotEmpty()) {
                    GroupChips(
                        groups = when (uiState.selectedGroupFilter) {
                            GroupFilter.ALL_GROUPS -> uiState.groups
                            GroupFilter.MANAGED_GROUPS -> uiState.managedGroups
                            GroupFilter.MEMBER_GROUPS -> uiState.memberGroups
                        },
                        selectedGroupId = uiState.selectedGroupId,
                        onGroupSelected = { viewModel.setGroupFilter(it) },
                        groupsWithManagePermission = uiState.groupsWithManagePermission
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    uiState.selectedGroupId != null -> "No tasks for this group yet"
                                    uiState.selectedGroupFilter == GroupFilter.MANAGED_GROUPS -> "No tasks in groups you manage"
                                    uiState.selectedGroupFilter == GroupFilter.MEMBER_GROUPS -> "No tasks in groups where you're a member"
                                    else -> "No group tasks yet"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Only show add task button if user has permission
                            if (uiState.selectedGroupId == null ||
                                viewModel.canManageTasksInGroup(uiState.selectedGroupId!!)) {
                                AssistChip(
                                    onClick = { viewModel.showAddTaskDialog() },
                                    label = { Text("Add a task") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }

                            if (uiState.selectedGroupFilter != GroupFilter.ALL_GROUPS || uiState.selectedGroupId != null) {
                                Spacer(modifier = Modifier.height(8.dp))
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
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredTasks) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Group name
                                    val group = uiState.groups.find { it.id == task.groupId }
                                    if (group != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Text(
                                                text = group.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )

                                            if (viewModel.canManageTasksInGroup(group.id)) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ManageAccounts,
                                                    contentDescription = "You can manage tasks",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Task details
                                    TaskItem(
                                        task = task,
                                        onClick = {
                                            navController.navigate("${Screen.GroupTaskDetail.route}/${task.id}")
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                        }
                    }
                }
            }
        }

        if (uiState.showAddTaskDialog) {
            // Filter groups to only show those where user has permission to add tasks
            val permittedGroups = uiState.groups.filter { group ->
                uiState.groupsWithManagePermission.contains(group.id)
            }

            AddGroupTaskDialog(
                onDismiss = { viewModel.hideAddTaskDialog() },
                onTaskAdded = { task ->
                    viewModel.addTask(task) {}
                },
                currentUserId = uiState.currentUserId,
                groups = permittedGroups,
                groupMembers = uiState.groupMembers,
                isLoading = uiState.isAddingTask
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupFilterPanel(
    activeFilter: GroupFilter,
    onFilterSelected: (GroupFilter) -> Unit,
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
                    text = "Filter by Group Type",
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
                    selected = activeFilter == GroupFilter.ALL_GROUPS,
                    onClick = { onFilterSelected(GroupFilter.ALL_GROUPS) },
                    label = { Text("All Groups") },
                    leadingIcon = if (activeFilter == GroupFilter.ALL_GROUPS) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == GroupFilter.MANAGED_GROUPS,
                    onClick = { onFilterSelected(GroupFilter.MANAGED_GROUPS) },
                    label = { Text("Groups I Manage") },
                    leadingIcon = if (activeFilter == GroupFilter.MANAGED_GROUPS) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = activeFilter == GroupFilter.MEMBER_GROUPS,
                    onClick = { onFilterSelected(GroupFilter.MEMBER_GROUPS) },
                    label = { Text("Groups I'm a Member") },
                    leadingIcon = if (activeFilter == GroupFilter.MEMBER_GROUPS) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun ActiveGroupFilterBar(
    groupFilter: GroupFilter,
    selectedGroup: Group?,
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

        if (groupFilter != GroupFilter.ALL_GROUPS) {
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
                        text = when (groupFilter) {
                            GroupFilter.ALL_GROUPS -> "All Groups"
                            GroupFilter.MANAGED_GROUPS -> "Groups I Manage"
                            GroupFilter.MEMBER_GROUPS -> "Groups I'm a Member"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (selectedGroup != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedGroup.name,
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

@Composable
fun GroupChips(
    groups: List<Group>,
    selectedGroupId: String?,
    onGroupSelected: (String?) -> Unit,
    groupsWithManagePermission: List<String>
) {
    if (groups.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Select Group",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All groups option
            FilterChip(
                selected = selectedGroupId == null,
                onClick = { onGroupSelected(null) },
                label = { Text("All") },
                leadingIcon = if (selectedGroupId == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )

            // Individual group chips
            groups.forEach { group ->
                val canManage = groupsWithManagePermission.contains(group.id)

                FilterChip(
                    selected = selectedGroupId == group.id,
                    onClick = { onGroupSelected(group.id) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(group.name)

                            if (canManage) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ManageAccounts,
                                    contentDescription = "You can manage tasks",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
