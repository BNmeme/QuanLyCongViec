package com.example.quanlycongviec.data.repository

import com.example.quanlycongviec.domain.model.Notification
import com.example.quanlycongviec.ui.screens.notifications.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    suspend fun getNotificationsForUser(userId: String): List<Notification> {
        val querySnapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Notification::class.java)?.copy(id = document.id)
        }
    }
    
    suspend fun markNotificationAsRead(notificationId: String) {
        firestore.collection("notifications").document(notificationId)
            .update("isRead", true)
            .await()
    }
    
    suspend fun createNotification(notification: Notification): String {
        val documentRef = firestore.collection("notifications").document()
        firestore.collection("notifications").document(documentRef.id).set(notification).await()
        return documentRef.id
    }
    
    suspend fun createTaskAssignedNotification(
        userId: String,
        taskTitle: String,
        taskId: String,
        assignedByName: String
    ): String {
        val notification = Notification(
            title = "New Task Assignment",
            message = "$assignedByName assigned you a task: $taskTitle",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            userId = userId,
            type = NotificationType.TASK_ASSIGNED,
            relatedTaskId = taskId
        )
        
        return createNotification(notification)
    }
    
    suspend fun createTaskCompletedNotification(
        userId: String,
        taskTitle: String,
        taskId: String,
        completedByName: String
    ): String {
        val notification = Notification(
            title = "Task Completed",
            message = "$completedByName completed task: $taskTitle",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            userId = userId,
            type = NotificationType.TASK_COMPLETED,
            relatedTaskId = taskId
        )
        
        return createNotification(notification)
    }
    
    suspend fun createGroupInvitationNotification(
        userId: String,
        groupName: String,
        groupId: String,
        invitedByName: String
    ): String {
        val notification = Notification(
            title = "Group Invitation",
            message = "$invitedByName invited you to join group: $groupName",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            userId = userId,
            type = NotificationType.GROUP_INVITATION,
            relatedGroupId = groupId
        )
        
        return createNotification(notification)
    }
}
