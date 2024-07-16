package com.example.timemanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson

data class QuizQuestion(
    val question: String,
    val answers: List<String>,
    val correctAnswer: String
)

data class Quiz(
    val questions: List<QuizQuestion>
)

fun parseQuiz(json: String): Quiz {
    val gson = Gson()
    return gson.fromJson(json, Quiz::class.java)
}

@Composable
fun QuizScreen(quiz: Quiz, navController: NavController) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var feedbackMessage by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var showScore by remember { mutableStateOf(false) }
    var clicked by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    val answeredCorrectly = remember { mutableStateOf(Array(quiz.questions.size) { false }) }

    val currentQuestion = quiz.questions[currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = currentQuestion.question, style = MaterialTheme.typography.h6)

        Spacer(modifier = Modifier.height(16.dp))

        currentQuestion.answers.forEach { answer ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = selectedAnswer == answer,
                    onClick = { selectedAnswer = answer }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = answer)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (selectedAnswer?.trim()?.equals(currentQuestion.correctAnswer.trim(), ignoreCase = true) == true) {
                if (clicked == 0) {
                    correctAnswers++
                }
                isCorrect = true
                feedbackMessage = "Correct!"
            } else {
                isCorrect = false
                clicked++
                feedbackMessage = "Incorrect!"
            }
        }) {
            Text(text = "Submit")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (feedbackMessage.isNotEmpty()) {
            Text(
                text = feedbackMessage,
                style = MaterialTheme.typography.h6,
                color = if (isCorrect) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.error
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isCorrect && currentQuestionIndex < quiz.questions.size - 1) {
            Button(onClick = {
                currentQuestionIndex++
                clicked = 0
                selectedAnswer = null
                feedbackMessage = ""
                isCorrect = false
            }) {
                Text(text = "Next Question")
            }
        }
        if (isCorrect && currentQuestionIndex == quiz.questions.size - 1) {
            Button(onClick = {
                showScore = true
            }) {
                Text(text = "See your score")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Correct Answers: $correctAnswers",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary
        )
    }

    if (showScore) {
        AlertDialog(
            onDismissRequest = { showScore = false },
            confirmButton = {
                TextButton(onClick = { navController.navigate(Routes.AutomaticQuizMakerPage) }) {
                    androidx.compose.material3.Text("OK")
                }
            },
            title = { androidx.compose.material3.Text("Your Score") },
            text = { androidx.compose.material3.Text("You got $correctAnswers out of ${quiz.questions.size} ") }
        )
    }
}