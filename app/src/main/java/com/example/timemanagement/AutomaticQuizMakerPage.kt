package com.example.timemanagement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AutomaticQuizMakerPage(navController: NavController){
    val scrollStateGemini = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollStateGemini)
    ) {
        Text(text = "Automatic Quiz Helper")
        Button(onClick = {navController.navigate(Routes.PhotoScreenPage)}) {
            Text(text = "Generate Quiz")
        }
    }
}

@Composable
fun PhotoScreenPage(){
    val scrollStateGemini = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollStateGemini)
    ) {
        PhotoPickerScreenGemini("You will get the topic or notes in a picture" +
                "analyze it and generate a quiz base on the amount of questions you get" +
                "Make the questions in json format. Each question will have 4 answer choice and then" +
                "at the bottom include the correct answer. Just give the JSON don't say anything else")
    }

}