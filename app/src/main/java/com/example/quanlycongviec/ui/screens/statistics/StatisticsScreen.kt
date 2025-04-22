package com.example.quanlycongviec.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.ui.theme.TaskPriority1
import com.example.quanlycongviec.ui.theme.TaskPriority2
import com.example.quanlycongviec.ui.theme.TaskPriority3

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            TaskCompletionCard(
                totalTasks = uiState.totalTasks,
                completedTasks = uiState.completedTasks
            )
        }
        
        item {
            PriorityDistributionCard(
                highPriorityCount = uiState.highPriorityCount,
                mediumPriorityCount = uiState.mediumPriorityCount,
                lowPriorityCount = uiState.lowPriorityCount
            )
        }
        
        item {
            TasksOverTimeCard(
                taskCountByDay = uiState.taskCountByDay
            )
        }
        
        item {
            TaskTypeDistributionCard(
                personalTaskCount = uiState.personalTaskCount,
                groupTaskCount = uiState.groupTaskCount
            )
        }
    }
}

@Composable
fun TaskCompletionCard(
    totalTasks: Int,
    completedTasks: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Task Completion",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val completionPercentage = if (totalTasks > 0) {
                (completedTasks.toFloat() / totalTasks.toFloat()) * 100
            } else {
                0f
            }

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val colorScheme = MaterialTheme.colorScheme // Access colorScheme here
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val radius = minOf(canvasWidth, canvasHeight) / 2
                    val center = Offset(canvasWidth / 2, canvasHeight / 2)

                    // Draw background circle
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 20f)
                    )

                    // Draw progress arc
                    val sweepAngle = (completionPercentage / 100) * 360f
                    drawArc(
                        color = colorScheme.primary, // Use colorScheme here
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 20f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${completionPercentage.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$completedTasks of $totalTasks tasks",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityDistributionCard(
    highPriorityCount: Int,
    mediumPriorityCount: Int,
    lowPriorityCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Task Priority Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PriorityBar(
                label = "High",
                count = highPriorityCount,
                color = TaskPriority1
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PriorityBar(
                label = "Medium",
                count = mediumPriorityCount,
                color = TaskPriority2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PriorityBar(
                label = "Low",
                count = lowPriorityCount,
                color = TaskPriority3
            )
        }
    }
}

@Composable
fun PriorityBar(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        
        LinearProgressIndicator(
            progress = if (count > 0) 1f else 0f,
            color = color,
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
        )
        
        Text(
            text = count.toString(),
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TasksOverTimeCard(
    taskCountByDay: Map<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tasks Over Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (taskCountByDay.isEmpty()) {
                Text(
                    text = "No task data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                // Simple representation of tasks over time
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // In a real app, you would implement a line chart here
                    // For simplicity, we're just showing a placeholder message
                    Text(
                        text = "Task trend visualization would be shown here",
                        modifier = Modifier.align(Alignment.Center),
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskTypeDistributionCard(
    personalTaskCount: Int,
    groupTaskCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Task Type Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TypePieSlice(
                    label = "Personal",
                    count = personalTaskCount,
                    color = colorScheme.primary
                )
                
                TypePieSlice(
                    label = "Group",
                    count = groupTaskCount,
                    color = colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun TypePieSlice(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
