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
//    val generativeModel = Firebase.vertexAI.generativeModel("gemini-1.5-flash-001")
//    val response = generativeModel.generateContent("Write a story about the green robot")
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
    ) {
        Text(text = "Homework Helper Page")
        PhotoPickerScreenGemini("You will analyze the question in the picture and" +
                "then provide step-by-step instructions to solve the problem. Make sure you explain " +
                "your reasoning and process.")
    }
}
