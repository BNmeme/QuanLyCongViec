package com.example.quanlycongviec.di

import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.data.repository.LabelRepository
import com.example.quanlycongviec.data.repository.NotificationRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AppModule {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth)
    }

    private val userRepository: UserRepository by lazy {
        UserRepository(firestore)
    }

    private val taskRepository: TaskRepository by lazy {
        TaskRepository(firestore)
    }

    private val groupRepository: GroupRepository by lazy {
        GroupRepository(firestore)
    }

    private val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(firestore)
    }

    private val labelRepository: LabelRepository by lazy {
        LabelRepository(firestore)
    }

    fun provideAuthRepository(): AuthRepository = authRepository

    fun provideUserRepository(): UserRepository = userRepository

    fun provideTaskRepository(): TaskRepository = taskRepository

    fun provideGroupRepository(): GroupRepository = groupRepository

    fun provideNotificationRepository(): NotificationRepository = notificationRepository

    fun provideLabelRepository(): LabelRepository = labelRepository
}
