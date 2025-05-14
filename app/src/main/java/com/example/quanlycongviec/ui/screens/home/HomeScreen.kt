package com.example.quanlycongviec.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.ui.components.TaskItem
import com.example.quanlycongviec.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

val PersonalTaskColor = Color(0xFF1976D2)
val GroupTaskColor = Color(0xFF2196F3)
val SuccessColor = Color(0xFF4CAF50)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
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
                SectionHeader(
                    title = "Today's Tasks",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            val todayTasks = uiState.recentTasks.filter {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val tomorrow = today + 24 * 60 * 60 * 1000

                it.dueDate in today until tomorrow
            }

            if (todayTasks.isNotEmpty()) {
                items(todayTasks) { task ->
                    TaskItem(
                        task = task,
                        onClick = {
                            navigateToTaskDetail(navController, task)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
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
                    SectionHeader(
                        title = "Recent Tasks",
                        modifier = Modifier
                    )

                    Button(
                        onClick = { viewModel.showAddTaskDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Task")
                    }
                }
            }

            if (uiState.recentTasks.isEmpty()) {
                item {
                    EmptyTasksMessage(message = "No recent tasks")
                }
            } else {
                items(uiState.recentTasks.take(5)) { task ->
                    TaskItem(
                        task = task,
                        onClick = {
                            navigateToTaskDetail(navController, task)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
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

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun UserGreetingSection(
    userName: String,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Hello,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
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
            .padding(horizontal = 16.dp)
            .offset(y = (-20).dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$completionPercentage",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                        )
                    }

                    Text(
                        text = "$completedTasksCount of $totalTasks tasks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(70.dp)
                ) {
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    CircularProgressIndicator(
                        progress = completionPercentage / 100f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TaskTypeCounter(
                    count = personalTasksCount,
                    label = "Personal",
                    color = PersonalTaskColor
                )

                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                TaskTypeCounter(
                    count = groupTasksCount,
                    label = "Group",
                    color = GroupTaskColor
                )

                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                TaskTypeCounter(
                    count = completedTasksCount,
                    label = "Completed",
                    color = SuccessColor
                )
            }
        }
    }
}

@Composable
fun TaskTypeCounter(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TaskCategoriesSection(
    onPersonalTasksClick: () -> Unit,
    onGroupTasksClick: () -> Unit,
    onGroupsClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCard(
                icon = Icons.Default.Person,
                title = "Personal Tasks",
                containerColor = PersonalTaskColor,
                contentColor = Color.White,
                onClick = onPersonalTasksClick,
                modifier = Modifier.weight(1f)
            )

            CategoryCard(
                icon = Icons.Default.Group,
                title = "Group Tasks",
                containerColor = GroupTaskColor,
                contentColor = Color.White,
                onClick = onGroupTasksClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CategoryCard(
            icon = Icons.Default.People,
            title = "Manage Groups",
            description = "Create and manage your groups",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
            .height(if (description == null) 110.dp else 130.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = contentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    color = contentColor.copy(alpha = 0.8f)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun navigateToTaskDetail(navController: NavController, task: Task) {
    if (task.isGroupTask) {
        navController.navigate("${Screen.GroupTaskDetail.route}/${task.id}")
    } else {
        navController.navigate("${Screen.PersonalTaskDetail.route}/${task.id}")
    }
}
