package com.example.quanlycongviec.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quanlycongviec.R
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.ui.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val authRepository = AppModule.provideAuthRepository()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_task),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
    }

    LaunchedEffect(key1 = true) {
        delay(1500) // Show splash for 1.5 seconds

        // Check if user is already logged in
        if (authRepository.isUserLoggedIn()) {
            // User is logged in, navigate to Home screen
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // User is not logged in, navigate to Sign In screen
            navController.navigate(Screen.SignIn.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }
}
