package com.example.timemanagement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AutomaticQuizMakerPage(navController: NavController){
    val scrollStateGemini = rememberScrollState()
    var showQuiz by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollStateGemini)
    ) {
        Text(text = "Automatic Quiz Helper")
        Button(onClick = {navController.navigate(Routes.PhotoScreenPage)}) {
            Text(text = "Generate Quiz")
        }
        if(responseFull.isNotEmpty()){
            Button(onClick = { showQuiz = true }) {
                Text(text = "Quiz")
            }
            if (showQuiz && responseFull.isNotEmpty()) {
                val jsonResponse = responseFull.trimIndent()
                val quiz = parseQuiz(jsonResponse)
                QuizScreen(quiz, navController)
            }
        }
    }
}

var responseFull : String = ""

@Composable
fun PhotoScreenPage(){
    val scrollStateGemini = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollStateGemini)
    ) {
        PhotoPickerScreenGemini("You will get the topic or notes in a picture analyze it and generate a quiz based on the amount of questions you get. " +
                "Make the questions in json format. Each question will have 4 answer choices and then at the bottom include the correct answer. " +
                "Here is an example:\n" +
                "{\n" +
                "    \"questions\": [\n" +
                "        {\n" +
                "            \"question\": \"What is the capital of France?\",\n" +
                "            \"answers\": [\"Berlin\", \"London\", \"Paris\", \"Madrid\"],\n" +
                "            \"correctAnswer\": \"Paris\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"What is 2 + 2?\",\n" +
                "            \"answers\": [\"3\", \"4\", \"5\", \"6\"],\n" +
                "            \"correctAnswer\": \"4\"\n" +
                "        }\n" +
                "    ]\n" +
                "}")
    }

}