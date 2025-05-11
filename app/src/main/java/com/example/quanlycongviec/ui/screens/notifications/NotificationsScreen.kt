package com.example.quanlycongviec.ui.screens.notifications

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Notification
import com.example.quanlycongviec.ui.navigation.Screen
import com.example.quanlycongviec.ui.screens.tasks.group.GroupInvitationResponseDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInvitationDialog by remember { mutableStateOf<Notification?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Log the current state for debugging
    LaunchedEffect(uiState) {
        Log.d("NotificationsScreen", "UI State: isLoading=${uiState.isLoading}, notifications=${uiState.notifications.size}, error=${uiState.errorMessage}")
    }

    // Show error message if any
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = MaterialTheme.colorScheme.onPrimary) },
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
                    IconButton(onClick = { viewModel.loadNotifications() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // For testing purposes only
            FloatingActionButton(
                onClick = {
                    viewModel.createTestNotification()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Test notification created")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Test Notification",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
        } else if (uiState.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No notifications yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(uiState.notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            // Only process click if it's not a responded invitation
                            if (notification.type == NotificationType.GROUP_INVITATION && notification.isResponded) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("You have already responded to this invitation")
                                }
                                return@NotificationItem
                            }

                            viewModel.markAsRead(notification.id)

                            when (notification.type) {
                                NotificationType.GROUP_INVITATION -> {
                                    showInvitationDialog = notification
                                }
                                NotificationType.TASK_ASSIGNED, NotificationType.TASK_DEADLINE -> {
                                    notification.relatedTaskId?.let { taskId ->
                                        if (notification.relatedGroupId != null) {
                                            navController.navigate("${Screen.GroupTaskDetail.route}/$taskId")
                                        } else {
                                            navController.navigate("${Screen.PersonalTaskDetail.route}/$taskId")
                                        }
                                    }
                                }
                                NotificationType.GROUP_INVITATION_ACCEPTED,
                                NotificationType.GROUP_INVITATION_DECLINED -> {
                                    notification.relatedGroupId?.let { groupId ->
                                        navController.navigate("${Screen.GroupDetail.route}/$groupId")
                                    }
                                }
                                else -> {
                                    // Just mark as read
                                }
                            }
                        }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        // Handle group invitation dialog
        showInvitationDialog?.let { notification ->
            val groupId = notification.relatedGroupId ?: return@let
            val groupName = notification.message.substringAfterLast(": ")
            val invitedByName = notification.message.substringBefore(" invited you")

            GroupInvitationResponseDialog(
                groupName = groupName,
                invitedByName = invitedByName,
                onAccept = {
                    // Accept invitation logic
                    coroutineScope.launch {
                        try {
                            val groupRepository = AppModule.provideGroupRepository()
                            val notificationRepository = AppModule.provideNotificationRepository()
                            val authRepository = AppModule.provideAuthRepository()
                            val userRepository = AppModule.provideUserRepository()

                            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                            val currentUser = userRepository.getUserById(currentUserId)

                            // Mark the notification as responded
                            viewModel.markInvitationAsResponded(notification.id)

                            // Accept the invitation
                            groupRepository.acceptGroupInvitation(groupId, currentUserId)

                            // Send notification to group creator
                            val group = groupRepository.getGroupById(groupId)
                            notificationRepository.createGroupInvitationResponseNotification(
                                userId = group.createdBy,
                                groupName = group.name,
                                groupId = groupId,
                                respondentName = currentUser?.name ?: "Someone",
                                accepted = true
                            )

                            // Navigate to the group detail screen
                            navController.navigate("${Screen.GroupDetail.route}/$groupId")
                        } catch (e: Exception) {
                            Log.e("NotificationsScreen", "Error accepting invitation: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error accepting invitation: ${e.message}")
                        }
                        showInvitationDialog = null
                    }
                },
                onDecline = {
                    // Decline invitation logic
                    coroutineScope.launch {
                        try {
                            val groupRepository = AppModule.provideGroupRepository()
                            val notificationRepository = AppModule.provideNotificationRepository()
                            val authRepository = AppModule.provideAuthRepository()
                            val userRepository = AppModule.provideUserRepository()

                            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                            val currentUser = userRepository.getUserById(currentUserId)

                            // Mark the notification as responded
                            viewModel.markInvitationAsResponded(notification.id)

                            // Decline the invitation
                            groupRepository.declineGroupInvitation(groupId, currentUserId)

                            // Send notification to group creator
                            val group = groupRepository.getGroupById(groupId)
                            notificationRepository.createGroupInvitationResponseNotification(
                                userId = group.createdBy,
                                groupName = group.name,
                                groupId = groupId,
                                respondentName = currentUser?.name ?: "Someone",
                                accepted = false
                            )

                            snackbarHostState.showSnackbar("Invitation declined")
                        } catch (e: Exception) {
                            Log.e("NotificationsScreen", "Error declining invitation: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error declining invitation: ${e.message}")
                        }
                        showInvitationDialog = null
                    }
                },
                onDismiss = {
                    showInvitationDialog = null
                }
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val isRead = notification.isRead
    val isResponded = notification.isResponded

    // Determine if this is a responded invitation
    val isRespondedInvitation = notification.type == NotificationType.GROUP_INVITATION && isResponded

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = !isRespondedInvitation)
            .alpha(if (isRead || isRespondedInvitation) 0.7f else 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification icon based on type
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getNotificationColor(notification.type).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isRead || isRespondedInvitation) FontWeight.Normal else FontWeight.Bold,
                    textDecoration = if (isRespondedInvitation) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isRespondedInvitation)
                        "${notification.message} (Responded)"
                    else
                        notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (!isRead && !isRespondedInvitation) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.TASK_ASSIGNED -> Icons.Default.Person
        NotificationType.TASK_COMPLETED -> Icons.Default.CheckCircle
        NotificationType.TASK_DEADLINE -> Icons.Default.Schedule
        NotificationType.GROUP_INVITATION,
        NotificationType.GROUP_INVITATION_ACCEPTED,
        NotificationType.GROUP_INVITATION_DECLINED -> Icons.Default.Group
    }
}

@Composable
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.TASK_ASSIGNED -> MaterialTheme.colorScheme.primary
        NotificationType.TASK_COMPLETED -> Color(0xFF4CAF50) // Green
        NotificationType.TASK_DEADLINE -> Color(0xFFF44336) // Red
        NotificationType.GROUP_INVITATION -> Color(0xFF2196F3) // Blue
        NotificationType.GROUP_INVITATION_ACCEPTED -> Color(0xFF4CAF50) // Green
        NotificationType.GROUP_INVITATION_DECLINED -> Color(0xFFF44336) // Red
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            format.format(date)
        }
    }
}
