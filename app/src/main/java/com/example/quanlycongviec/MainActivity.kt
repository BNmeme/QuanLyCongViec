package com.example.quanlycongviec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.quanlycongviec.ui.navigation.AppNavHost
import com.example.quanlycongviec.ui.screens.main.MainScreen
import com.example.quanlycongviec.ui.theme.TaskManagerTheme
import com.example.quanlycongviec.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load dark mode preference on app start
        val isDarkMode = TaskManagerApplication.isDarkModeEnabled()
        ThemeManager.setDarkTheme(isDarkMode)

        setContent {
            TaskManagerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use MainScreen instead of AppNavHost directly
                    MainScreen()
                }
            }
        }
    }
}
