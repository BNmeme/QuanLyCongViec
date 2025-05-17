package com.example.quanlycongviec.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.ui.theme.*
import kotlin.math.min

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (uiState.errorMessage != null) {
            ErrorMessage(message = uiState.errorMessage!!)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TaskCompletionCard(
                        totalTasks = uiState.totalTasks,
                        completedTasks = uiState.completedTasks
                    )
                }

                item {
                    ProductivityScoreCard(
                        completionRate = if (uiState.totalTasks > 0) {
                            (uiState.completedTasks.toFloat() / uiState.totalTasks.toFloat()) * 100
                        } else {
                            0f
                        },
                        onTimeCompletionRate = uiState.onTimeCompletionRate,
                        priorityManagementScore = calculatePriorityScore(
                            uiState.highPriorityCount,
                            uiState.mediumPriorityCount,
                            uiState.lowPriorityCount,
                            uiState.completedHighPriorityCount,
                            uiState.completedMediumPriorityCount,
                            uiState.completedLowPriorityCount
                        )
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
                    TaskTypeDistributionCard(
                        personalTaskCount = uiState.personalTaskCount,
                        groupTaskCount = uiState.groupTaskCount
                    )
                }

                item {
                    TaskActivityHeatmapCard(
                        activityByDayOfWeek = uiState.activityByDayOfWeek
                    )
                }

                item {
                    TaskCompletionTimeCard(
                        averageCompletionTime = uiState.averageCompletionTime
                    )
                }

                item {
                    OverdueTasksCard(
                        overdueTasksCount = uiState.overdueTasksCount,
                        totalTasksWithDueDate = uiState.totalTasksWithDueDate
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCompletionCard(
    totalTasks: Int,
    completedTasks: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Task Completion",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val completionPercentage = if (totalTasks > 0) {
                (completedTasks.toFloat() / totalTasks.toFloat()) * 100
            } else {
                0f
            }

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val radius = min(canvasWidth, canvasHeight) / 2
                    val center = Offset(canvasWidth / 2, canvasHeight / 2)
                    val strokeWidth = 20f

                    // Draw background circle
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Draw progress arc
                    val sweepAngle = (completionPercentage / 100) * 360f
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${completionPercentage.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "$completedTasks of $totalTasks",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedTasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${totalTasks - completedTasks}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (totalTasks - completedTasks > 0) MaterialTheme.colorScheme.error else Color.Gray
                    )
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProductivityScoreCard(
    completionRate: Float,
    onTimeCompletionRate: Float,
    priorityManagementScore: Float
) {
    // Calculate overall productivity score (weighted average)
    val productivityScore = (completionRate * 0.4f + onTimeCompletionRate * 0.3f + priorityManagementScore * 0.3f).toInt()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Productivity Score",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Productivity score
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$productivityScore",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(productivityScore)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Score breakdown
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ScoreItem(
                    label = "Task Completion",
                    score = completionRate.toInt(),
                    weight = "40%"
                )

                Spacer(modifier = Modifier.height(8.dp))

                ScoreItem(
                    label = "On-time Completion",
                    score = onTimeCompletionRate.toInt(),
                    weight = "30%"
                )

                Spacer(modifier = Modifier.height(8.dp))

                ScoreItem(
                    label = "Priority Management",
                    score = priorityManagementScore.toInt(),
                    weight = "30%"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Productivity rating
            Text(
                text = getProductivityRating(productivityScore),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = getScoreColor(productivityScore),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ScoreItem(
    label: String,
    score: Int,
    weight: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$score",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = getScoreColor(score)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = weight,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun PriorityDistributionCard(
    highPriorityCount: Int,
    mediumPriorityCount: Int,
    lowPriorityCount: Int
) {
    val total = highPriorityCount + mediumPriorityCount + lowPriorityCount

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Task Priority Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (total == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityBar(
                        label = "High",
                        count = highPriorityCount,
                        total = total,
                        color = TaskPriority1
                    )

                    PriorityBar(
                        label = "Medium",
                        count = mediumPriorityCount,
                        total = total,
                        color = TaskPriority2
                    )

                    PriorityBar(
                        label = "Low",
                        count = lowPriorityCount,
                        total = total,
                        color = TaskPriority3
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0

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
            progress = if (total > 0) count.toFloat() / total else 0f,
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Text(
            text = "$count ($percentage%)",
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TaskTypeDistributionCard(
    personalTaskCount: Int,
    groupTaskCount: Int
) {
    val total = personalTaskCount + groupTaskCount
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryColor07 = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Task Type Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (total == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                // Pie chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val radius = min(canvasWidth, canvasHeight) / 3
                        val center = Offset(canvasWidth / 2, canvasHeight / 2)

                        val personalAngle = 360f * (personalTaskCount.toFloat() / total)

                        // Draw personal tasks slice
                        drawArc(
                            color = primaryColor,
                            startAngle = 0f,
                            sweepAngle = personalAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )

                        // Draw group tasks slice
                        drawArc(
                            color = tertiaryColor,
                            startAngle = personalAngle,
                            sweepAngle = 360f - personalAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val personalPercentage = if (total > 0) (personalTaskCount.toFloat() / total * 100).toInt() else 0
                    val groupPercentage = if (total > 0) (groupTaskCount.toFloat() / total * 100).toInt() else 0

                    TypeItem(
                        label = "Personal",
                        count = personalTaskCount,
                        percentage = personalPercentage,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TypeItem(
                        label = "Group",
                        count = groupTaskCount,
                        percentage = groupPercentage,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun TypeItem(
    label: String,
    count: Int,
    percentage: Int,
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
            text = "$count ($percentage%)",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TaskActivityHeatmapCard(
    activityByDayOfWeek: Map<Int, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarViewWeek,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Activity Pattern",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activityByDayOfWeek.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No activity data available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                val maxActivity = activityByDayOfWeek.values.maxOrNull() ?: 0
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

                // Heatmap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 0..6) {
                        val activity = activityByDayOfWeek[i] ?: 0
                        val intensity = if (maxActivity > 0) activity.toFloat() / maxActivity else 0f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.1f + (intensity * 0.9f)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$activity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (intensity > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = daysOfWeek[i],
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Most productive day
                val mostProductiveDay = activityByDayOfWeek.entries.maxByOrNull { it.value }
                if (mostProductiveDay != null && mostProductiveDay.value > 0) {
                    Text(
                        text = "Most productive day: ${daysOfWeek[mostProductiveDay.key]}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCompletionTimeCard(
    averageCompletionTime: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Task Completion Time",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (averageCompletionTime <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No completion time data available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatCompletionTime(averageCompletionTime),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Average time to complete tasks",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Completion time rating
                    val completionTimeRating = getCompletionTimeRating(averageCompletionTime)
                    Text(
                        text = completionTimeRating.first,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = completionTimeRating.second
                    )
                }
            }
        }
    }
}

@Composable
fun OverdueTasksCard(
    overdueTasksCount: Int,
    totalTasksWithDueDate: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Overdue Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalTasksWithDueDate == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks with due dates",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                val overduePercentage = if (totalTasksWithDueDate > 0) {
                    (overdueTasksCount.toFloat() / totalTasksWithDueDate.toFloat()) * 100
                } else {
                    0f
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$overdueTasksCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (overdueTasksCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Overdue tasks out of $totalTasksWithDueDate with due dates",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = overduePercentage / 100,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (overduePercentage > 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${overduePercentage.toInt()}% of tasks are overdue",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (overduePercentage > 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper functions
fun calculatePriorityScore(
    highCount: Int,
    mediumCount: Int,
    lowCount: Int,
    completedHighCount: Int,
    completedMediumCount: Int,
    completedLowCount: Int
): Float {
    // Calculate completion rate for each priority
    val highCompletionRate = if (highCount > 0) completedHighCount.toFloat() / highCount else 1f
    val mediumCompletionRate = if (mediumCount > 0) completedMediumCount.toFloat() / mediumCount else 1f
    val lowCompletionRate = if (lowCount > 0) completedLowCount.toFloat() / lowCount else 1f

    // Weight the completion rates by priority (high priority tasks are more important)
    val totalTasks = highCount + mediumCount + lowCount
    if (totalTasks == 0) return 100f

    val weightedScore = (highCompletionRate * highCount * 3 +
            mediumCompletionRate * mediumCount * 2 +
            lowCompletionRate * lowCount) /
            (highCount * 3 + mediumCount * 2 + lowCount)

    return weightedScore * 100
}

fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFFFFC107) // Yellow
        score >= 40 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

fun getProductivityRating(score: Int): String {
    return when {
        score >= 90 -> "Outstanding"
        score >= 80 -> "Excellent"
        score >= 70 -> "Very Good"
        score >= 60 -> "Good"
        score >= 50 -> "Average"
        score >= 40 -> "Below Average"
        score >= 30 -> "Needs Improvement"
        else -> "Poor"
    }
}

fun formatCompletionTime(hours: Float): String {
    val wholeHours = hours.toInt()
    val minutes = ((hours - wholeHours) * 60).toInt()

    return when {
        wholeHours > 0 && minutes > 0 -> "$wholeHours hr $minutes min"
        wholeHours > 0 -> "$wholeHours hr"
        else -> "$minutes min"
    }
}

fun getCompletionTimeRating(hours: Float): Pair<String, Color> {
    return when {
        hours < 24 -> Pair("Fast", Color(0xFF4CAF50)) // Green
        hours < 48 -> Pair("Good", Color(0xFF8BC34A)) // Light Green
        hours < 72 -> Pair("Average", Color(0xFFFFC107)) // Yellow
        hours < 96 -> Pair("Slow", Color(0xFFFF9800)) // Orange
        else -> Pair("Very Slow", Color(0xFFF44336)) // Red
    }
}
