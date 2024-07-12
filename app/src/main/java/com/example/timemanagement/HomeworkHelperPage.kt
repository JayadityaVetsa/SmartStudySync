package com.example.timemanagement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HomeworkHelperPage(navController: NavController){
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
    ) {
        AppHeader("Homework Helper")
        PhotoPickerScreenGeminiHomework(
            "You will analyze the question in the picture and" +
                "then provide step-by-step instructions to solve the problem. Make sure you explain " +
                "your reasoning and process. First walk the user through your thought process briefly (1-2 sentences for each step)" +
                "Give the answer and then give a more thorough explanation. ")
    }
}
