package com.example.quanlycongviec.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome, ${user?.email}", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}
