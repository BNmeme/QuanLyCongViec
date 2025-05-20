package com.example.quanlycongviec.data.repository

import android.util.Log
import com.example.quanlycongviec.domain.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.quanlycongviec.di.AppModule

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getPersonalTasks(userId: String): List<Task> {
        Log.d("TaskRepository", "Getting personal tasks for user: $userId")

        val querySnapshot = firestore.collection("tasks")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isGroupTask", false)
            .get()
            .await()

        val tasks = querySnapshot.documents.mapNotNull { document ->
            try {
                val task = document.toObject(Task::class.java)?.copy(id = document.id)
                Log.d("TaskRepository", "Loaded task: ${task?.title}, ID: ${task?.id}")
                task
            } catch (e: Exception) {
                Log.e("TaskRepository", "Error parsing task document: ${document.id}", e)
                null
            }
        }

        Log.d("TaskRepository", "Loaded ${tasks.size} personal tasks")
        return tasks
    }

    suspend fun getGroupTasksForUser(userId: String): List<Task> {
        // Get tasks where the user is assigned
        val querySnapshot = firestore.collection("tasks")
            .whereEqualTo("isGroupTask", true)
            .whereArrayContains("assignedTo", userId)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Task::class.java)?.copy(id = document.id)
        }
    }

    suspend fun getTasksByGroupId(groupId: String): List<Task> {
        val querySnapshot = firestore.collection("tasks")
            .whereEqualTo("groupId", groupId)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Task::class.java)?.copy(id = document.id)
        }
    }

    suspend fun getTaskById(taskId: String): Task {
        val document = firestore.collection("tasks").document(taskId).get().await()
        return document.toObject(Task::class.java)?.copy(id = document.id)
            ?: throw IllegalStateException("Task not found")
    }

    suspend fun createTask(task: Task): String {
        try {
            Log.d("TaskRepository", "Creating task: ${task.title} for user: ${task.userId}")
            val documentRef = firestore.collection("tasks").document()
            val taskWithId = task.copy(id = documentRef.id)
            Log.d("TaskRepository", "Task data to save: $taskWithId")
            firestore.collection("tasks").document(documentRef.id).set(taskWithId).await()
            Log.d("TaskRepository", "Task created with ID: ${documentRef.id}")

            // Create deadline notifications if due date is set
            if (task.dueDate > 0) {
                val notificationRepository = AppModule.provideNotificationRepository()

                // For personal tasks
                if (!task.isGroupTask) {
                    notificationRepository.createTaskDeadlineNotification(
                        userId = task.userId,
                        taskTitle = task.title,
                        taskId = documentRef.id,
                        dueDate = task.dueDate
                    )
                }
                // For group tasks
                else {
                    val authRepository = AppModule.provideAuthRepository()
                    val userRepository = AppModule.provideUserRepository()
                    val currentUserId = authRepository.getCurrentUserId() ?: ""
                    val currentUser = userRepository.getUserById(currentUserId)
                    val assignerName = currentUser?.name ?: "A team member"

                    for (assignedUserId in task.assignedTo) {
                        // Create deadline notification for each assigned user
                        notificationRepository.createTaskDeadlineNotification(
                            userId = assignedUserId,
                            taskTitle = task.title,
                            taskId = documentRef.id,
                            dueDate = task.dueDate,
                            groupId = task.groupId
                        )

                        // Create task assignment notification for each assigned user (except the creator)
                        if (assignedUserId != currentUserId) {
                            notificationRepository.createTaskAssignedNotification(
                                userId = assignedUserId,
                                taskTitle = task.title,
                                taskId = documentRef.id,
                                assignedByName = assignerName,
                                groupId = task.groupId
                            )
                        }
                    }
                }
            }

            return documentRef.id
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error creating task", e)
            throw e
        }
    }

    suspend fun updateTask(task: Task) {
        firestore.collection("tasks").document(task.id).set(task).await()

        // Update deadline notifications if due date is changed
        if (task.dueDate > 0) {
            val notificationRepository = AppModule.provideNotificationRepository()

            // For personal tasks
            if (!task.isGroupTask) {
                notificationRepository.createTaskDeadlineNotification(
                    userId = task.userId,
                    taskTitle = task.title,
                    taskId = task.id,
                    dueDate = task.dueDate
                )
            }
            // For group tasks
            else {
                for (assignedUserId in task.assignedTo) {
                    notificationRepository.createTaskDeadlineNotification(
                        userId = assignedUserId,
                        taskTitle = task.title,
                        taskId = task.id,
                        dueDate = task.dueDate,
                        groupId = task.groupId
                    )
                }
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        // Delete the task's notifications first
        val notificationRepository = AppModule.provideNotificationRepository()
        notificationRepository.deleteNotificationsForTask(taskId)

        // Then delete the task
        firestore.collection("tasks").document(taskId).delete().await()
    }

    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        val task = getTaskById(taskId)

        firestore.collection("tasks").document(taskId)
            .update("isCompleted", isCompleted)
            .await()

        // Create completion notification for group tasks
        if (isCompleted && task.isGroupTask) {
            val notificationRepository = AppModule.provideNotificationRepository()
            val userRepository = AppModule.provideUserRepository()
            val authRepository = AppModule.provideAuthRepository()

            val currentUserId = authRepository.getCurrentUserId() ?: return
            val currentUser = userRepository.getUserById(currentUserId)

            // Notify all assigned members except the current user
            for (assignedUserId in task.assignedTo) {
                if (assignedUserId != currentUserId) {
                    notificationRepository.createTaskCompletedNotification(
                        userId = assignedUserId,
                        taskTitle = task.title,
                        taskId = task.id,
                        completedByName = currentUser?.name ?: "A team member",
                        groupId = task.groupId
                    )
                }
            }
        }
    }

    suspend fun reassignTask(taskId: String, assignedTo: List<String>) {
        // When reassigning, we need to reset completion confirmations for users who are no longer assigned
        val task = getTaskById(taskId)
        val updatedConfirmations = task.completionConfirmations.filterKeys { userId ->
            assignedTo.contains(userId)
        }

        // Get previously assigned users and newly assigned users
        val previouslyAssigned = task.assignedTo
        val newlyAssigned = assignedTo.filter { userId -> !previouslyAssigned.contains(userId) }

        // Update the task
        firestore.collection("tasks").document(taskId)
            .update(
                mapOf(
                    "assignedTo" to assignedTo,
                    "completionConfirmations" to updatedConfirmations
                )
            )
            .await()

        // Send notifications to newly assigned users
        if (newlyAssigned.isNotEmpty()) {
            val notificationRepository = AppModule.provideNotificationRepository()
            val userRepository = AppModule.provideUserRepository()
            val authRepository = AppModule.provideAuthRepository()

            val currentUserId = authRepository.getCurrentUserId() ?: return
            val currentUser = userRepository.getUserById(currentUserId)
            val assignerName = currentUser?.name ?: "A team member"

            for (userId in newlyAssigned) {
                notificationRepository.createTaskAssignedNotification(
                    userId = userId,
                    taskTitle = task.title,
                    taskId = task.id,
                    assignedByName = assignerName,
                    groupId = task.groupId
                )
            }
        }
    }

    suspend fun deleteTasksByGroupId(groupId: String) {
        val tasks = getTasksByGroupId(groupId)
        for (task in tasks) {
            deleteTask(task.id)
        }
    }

    suspend fun confirmTaskCompletion(taskId: String, userId: String, isConfirmed: Boolean) {
        val task = getTaskById(taskId)
        val updatedConfirmations = task.completionConfirmations.toMutableMap()
        updatedConfirmations[userId] = isConfirmed

        firestore.collection("tasks").document(taskId)
            .update("completionConfirmations", updatedConfirmations)
            .await()
    }

    suspend fun resetAllCompletionConfirmations(taskId: String) {
        val task = getTaskById(taskId)
        val emptyConfirmations = task.assignedTo.associateWith { false }

        firestore.collection("tasks").document(taskId)
            .update("completionConfirmations", emptyConfirmations)
            .await()
    }
}
