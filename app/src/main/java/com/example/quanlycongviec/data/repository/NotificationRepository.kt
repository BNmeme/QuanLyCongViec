package com.example.quanlycongviec.data.repository

import android.util.Log
import com.example.quanlycongviec.domain.model.Notification
import com.example.quanlycongviec.ui.screens.notifications.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getNotificationsForUser(userId: String): List<Notification> {
        try {
            // Removed the orderBy clause as requested
            val querySnapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            Log.d("NotificationRepository", "Found ${querySnapshot.documents.size} notifications for user $userId")

            return querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        Log.d("NotificationRepository", "Document data: $data")

                        // Manually create Notification object from document data
                        val notification = Notification(
                            id = document.id,
                            userId = data["userId"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            timestamp = (data["timestamp"] as? Long) ?: 0L,
                            isRead = (data["isRead"] as? Boolean) ?: false,
                            type = try {
                                val typeStr = data["type"] as? String
                                if (typeStr != null) NotificationType.valueOf(typeStr) else NotificationType.TASK_ASSIGNED
                            } catch (e: Exception) {
                                Log.e("NotificationRepository", "Error parsing type: ${e.message}")
                                NotificationType.TASK_ASSIGNED
                            },
                            relatedTaskId = data["relatedTaskId"] as? String,
                            relatedGroupId = data["relatedGroupId"] as? String,
                            isResponded = (data["isResponded"] as? Boolean) ?: false
                        )

                        Log.d("NotificationRepository", "Parsed notification: $notification")
                        notification
                    } else {
                        Log.e("NotificationRepository", "Document data is null for document: ${document.id}")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("NotificationRepository", "Error parsing notification: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error getting notifications: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            Log.d("NotificationRepository", "Marked notification $notificationId as read")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking notification as read: ${e.message}", e)
        }
    }

    suspend fun markInvitationAsResponded(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "isResponded" to true
                    )
                )
                .await()
            Log.d("NotificationRepository", "Marked invitation $notificationId as responded")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking invitation as responded: ${e.message}", e)
        }
    }

    suspend fun createGroupInvitationNotification(
        userId: String,
        groupName: String,
        groupId: String,
        invitedByName: String
    ): String {
        try {
            val notificationId = UUID.randomUUID().toString()
            val notification = hashMapOf(
                "userId" to userId,
                "title" to "Group Invitation",
                "message" to "$invitedByName invited you to join group: $groupName",
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "type" to NotificationType.GROUP_INVITATION.name,
                "relatedGroupId" to groupId,
                "isResponded" to false
            )

            // Log before creating the notification
            Log.d("NotificationRepository", "Creating group invitation notification: $notification")

            firestore.collection("notifications").document(notificationId)
                .set(notification)
                .await()

            Log.d("NotificationRepository", "Successfully created group invitation notification with ID: $notificationId")
            return notificationId
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating group invitation notification: ${e.message}", e)
            throw e
        }
    }

    suspend fun createGroupInvitationResponseNotification(
        userId: String,
        groupName: String,
        groupId: String,
        respondentName: String,
        accepted: Boolean
    ) {
        try {
            val type = if (accepted) NotificationType.GROUP_INVITATION_ACCEPTED else NotificationType.GROUP_INVITATION_DECLINED
            val action = if (accepted) "accepted" else "declined"

            val notificationId = UUID.randomUUID().toString()
            val notification = hashMapOf(
                "userId" to userId,
                "title" to "Group Invitation Response",
                "message" to "$respondentName has $action your invitation to join $groupName",
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "type" to type.name,
                "relatedGroupId" to groupId,
                "isResponded" to false  // Not applicable for this type
            )

            firestore.collection("notifications").document(notificationId)
                .set(notification)
                .await()

            Log.d("NotificationRepository", "Created group invitation response notification: $notification")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating group invitation response notification: ${e.message}", e)
        }
    }

    suspend fun createTaskAssignedNotification(
        userId: String,
        taskTitle: String,
        taskId: String,
        assignedByName: String,
        groupId: String? = null
    ) {
        try {
            val groupText = if (groupId != null) " in your group" else ""

            val notificationId = UUID.randomUUID().toString()
            val notification = hashMapOf(
                "userId" to userId,
                "title" to "New Task Assigned",
                "message" to "$assignedByName assigned you a task: $taskTitle$groupText",
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "type" to NotificationType.TASK_ASSIGNED.name,
                "relatedTaskId" to taskId,
                "relatedGroupId" to groupId,
                "isResponded" to false  // Not applicable for this type
            )

            firestore.collection("notifications").document(notificationId)
                .set(notification)
                .await()

            Log.d("NotificationRepository", "Created task assigned notification: $notification")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating task assigned notification: ${e.message}", e)
        }
    }

    suspend fun createTaskDeadlineNotification(
        userId: String,
        taskTitle: String,
        taskId: String,
        dueDate: Long,
        groupId: String? = null
    ) {
        try {
            val notificationId = UUID.randomUUID().toString()
            val notification = hashMapOf(
                "userId" to userId,
                "title" to "Task Deadline",
                "message" to "Your task '$taskTitle' is due soon",
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "type" to NotificationType.TASK_DEADLINE.name,
                "relatedTaskId" to taskId,
                "relatedGroupId" to groupId,
                "isResponded" to false  // Not applicable for this type
            )

            firestore.collection("notifications").document(notificationId)
                .set(notification)
                .await()

            Log.d("NotificationRepository", "Created task deadline notification: $notification")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating task deadline notification: ${e.message}", e)
        }
    }

    suspend fun createTaskCompletedNotification(
        userId: String,
        taskTitle: String,
        taskId: String,
        completedByName: String,
        groupId: String? = null
    ) {
        try {
            val notificationId = UUID.randomUUID().toString()
            val notification = hashMapOf(
                "userId" to userId,
                "title" to "Task Completed",
                "message" to "$completedByName has completed the task: $taskTitle",
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "type" to NotificationType.TASK_COMPLETED.name,
                "relatedTaskId" to taskId,
                "relatedGroupId" to groupId,
                "isResponded" to false  // Not applicable for this type
            )

            firestore.collection("notifications").document(notificationId)
                .set(notification)
                .await()

            Log.d("NotificationRepository", "Created task completed notification: $notification")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating task completed notification: ${e.message}", e)
        }
    }

    suspend fun deleteNotificationsForTask(taskId: String) {
        try {
            val querySnapshot = firestore.collection("notifications")
                .whereEqualTo("relatedTaskId", taskId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                firestore.collection("notifications").document(document.id).delete().await()
            }

            Log.d("NotificationRepository", "Deleted notifications for task: $taskId")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error deleting notifications for task: ${e.message}", e)
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .delete()
                .await()
            Log.d("NotificationRepository", "Deleted notification $notificationId")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error deleting notification: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteNotificationsForGroup(groupId: String) {
        try {
            val querySnapshot = firestore.collection("notifications")
                .whereEqualTo("relatedGroupId", groupId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                firestore.collection("notifications").document(document.id).delete().await()
            }

            Log.d("NotificationRepository", "Deleted notifications for group: $groupId")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error deleting notifications for group: ${e.message}", e)
        }
    }
}
