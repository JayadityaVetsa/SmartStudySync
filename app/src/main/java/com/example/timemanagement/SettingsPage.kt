package com.example.timemanagement

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsPage() {
    var user by remember { mutableStateOf<FirebaseUser?>(null) }

    LaunchedEffect(Unit) {
        user = FirebaseAuth.getInstance().currentUser
    }

    if (user != null) {
        Column {
            Text(text = "Settings")
            Text(text = "User: ${user!!.email}")
        }
    } else {
        Text(text = "Loading user information...")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsPage() {
    SettingsPage()
}
