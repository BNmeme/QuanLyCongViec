package com.example.quanlycongviec.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.ui.components.TaskItem
import com.example.quanlycongviec.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Effect to refresh data when screen is shown
    LaunchedEffect(key1 = true) {
        viewModel.refreshData()
    }

    Scaffold(
        floatingActionButton = {
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // User greeting
                item {
                    UserGreetingSection(
                        userName = uiState.user?.name ?: "User",
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

                // Task summary
                item {
                    TaskSummarySection(
                        personalTasksCount = uiState.personalTasks.size,
                        groupTasksCount = uiState.groupTasks.size,
                        completedTasksCount = uiState.personalTasks.count { it.isCompleted } + uiState.groupTasks.count { it.isCompleted }
                    )
                }

                // Task categories
                item {
                    TaskCategoriesSection(
                        onPersonalTasksClick = { navController.navigate(Screen.PersonalTasks.route) },
                        onGroupTasksClick = { navController.navigate(Screen.GroupTasks.route) },
                        onGroupsClick = { navController.navigate(Screen.Groups.route) }
                    )
                }

                // Today's tasks
                item {
                    Text(
                        text = "Today's Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
                    )
                }

                if (uiState.todayTasks.isNotEmpty()) {
                    items(uiState.todayTasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = {
                                navigateToTaskDetail(navController, task)
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                } else {
                    item {
                        EmptyTasksMessage(message = "No tasks due today")
                    }
                }

                // Recent tasks
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recent Tasks",
                            style = MaterialTheme.typography.titleLarge
                        )

                        TextButton(onClick = { viewModel.showAddTaskDialog() }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Text(text = "Add Task")
                        }
                    }
                }

                if (uiState.recentTasks.isEmpty()) {
                    item {
                        EmptyTasksMessage(message = "No recent tasks")
                    }
                } else {
                    items(uiState.recentTasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = {
                                navigateToTaskDetail(navController, task)
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Add some bottom padding
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        if (uiState.showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { viewModel.hideAddTaskDialog() },
                onAddPersonalTask = {
                    viewModel.hideAddTaskDialog()
                    navController.navigate(Screen.PersonalTasks.route)
                },
                onAddGroupTask = {
                    viewModel.hideAddTaskDialog()
                    navController.navigate(Screen.GroupTasks.route)
                }
            )
        }
    }
}

@Composable
fun UserGreetingSection(
    userName: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Hello,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TaskSummarySection(
    personalTasksCount: Int,
    groupTasksCount: Int,
    completedTasksCount: Int
) {
    val totalTasks = personalTasksCount + groupTasksCount
    val completionPercentage = if (totalTasks > 0) {
        (completedTasksCount * 100) / totalTasks
    } else {
        0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$completionPercentage%",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$completedTasksCount of $totalTasks tasks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                CircularProgressIndicator(
                    progress = completionPercentage / 100f,
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 8.dp
                )
            }
        }
    }
}

@Composable
fun TaskCategoriesSection(
    onPersonalTasksClick: () -> Unit,
    onGroupTasksClick: () -> Unit,
    onGroupsClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCard(
                icon = Icons.Default.Person,
                title = "Personal Tasks",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onPersonalTasksClick,
                modifier = Modifier.weight(1f)
            )

            CategoryCard(
                icon = Icons.Default.Group,
                title = "Group Tasks",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onGroupTasksClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CategoryCard(
            icon = Icons.Default.People,
            title = "Manage Groups",
            description = "Create and manage your groups",
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onGroupsClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CategoryCard(
    icon: ImageVector,
    title: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Card(
        modifier = modifier
            .height(if (description == null) 100.dp else 120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmptyTasksMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun navigateToTaskDetail(navController: NavController, task: Task) {
    if (task.isGroupTask) {
        navController.navigate("${Screen.GroupTaskDetail.route}/${task.id}")
    } else {
        navController.navigate("${Screen.PersonalTaskDetail.route}/${task.id}")
    }
}
