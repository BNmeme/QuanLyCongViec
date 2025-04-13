package com.example.quanlycongviec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.quanlycongviec.View.AppNavGraph
import com.example.quanlycongviec.View.LoginScreen
import com.example.quanlycongviec.View.RegisterScreen
import com.example.quanlycongviec.View.Screen
import com.example.quanlycongviec.ui.theme.QuanLyCongViecTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val user = FirebaseAuth.getInstance().currentUser
        val startDest = if (user != null) Screen.Home.route else Screen.Auth.route

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppNavGraph(startDestination = startDest)
            }
        }
    }
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }

    if (isLogin) {
        LoginScreen(onToggle = { isLogin = false }, onLoginSuccess = onLoginSuccess)
    } else {
        RegisterScreen(onToggle = { isLogin = true }, onRegisterSuccess = onLoginSuccess)
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QuanLyCongViecTheme {

    }
}