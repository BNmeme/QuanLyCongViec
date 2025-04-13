package com.example.quanlycongviec.View

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quanlycongviec.AuthScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
}

@Composable
fun AppNavGraph(startDestination: String = Screen.Auth.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            })
        }
    }
}
