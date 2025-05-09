package com.example.quanlycongviec.data.repository

import android.util.Log
import com.example.quanlycongviec.domain.model.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
            return documentRef.id
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error creating task", e)
            throw e
        }
    }

    suspend fun updateTask(task: Task) {
        firestore.collection("tasks").document(task.id).set(task).await()
    }

    suspend fun deleteTask(taskId: String) {
        firestore.collection("tasks").document(taskId).delete().await()
    }

    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        firestore.collection("tasks").document(taskId)
            .update("isCompleted", isCompleted)
            .await()
    }

    suspend fun reassignTask(taskId: String, assignedTo: List<String>) {
        // When reassigning, we need to reset completion confirmations for users who are no longer assigned
        val task = getTaskById(taskId)
        val updatedConfirmations = task.completionConfirmations.filterKeys { userId ->
            assignedTo.contains(userId)
        }

        firestore.collection("tasks").document(taskId)
            .update(
                mapOf(
                    "assignedTo" to assignedTo,
                    "completionConfirmations" to updatedConfirmations
                )
            )
            .await()
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
