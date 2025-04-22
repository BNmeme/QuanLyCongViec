package com.example.quanlycongviec.di

import com.example.quanlycongviec.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AppModule {

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val authRepository by lazy { AuthRepository(firebaseAuth, firestore) }
    private val userRepository by lazy { UserRepository(firestore) }
    private val taskRepository by lazy { TaskRepository(firestore) }
    private val groupRepository by lazy { GroupRepository(firestore) }
    private val notificationRepository by lazy { NotificationRepository(firestore) }

    fun provideAuthRepository(): AuthRepository = authRepository

    fun provideUserRepository(): UserRepository = userRepository

    fun provideTaskRepository(): TaskRepository = taskRepository

    fun provideGroupRepository(): GroupRepository = groupRepository

    fun provideNotificationRepository(): NotificationRepository = notificationRepository
}
