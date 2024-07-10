package com.example.timemanagement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

@Composable
fun AutomaticQuizMakerPage(navController: NavController){
    val scrollStateGemini = rememberScrollState()
    var quizzes by remember { mutableStateOf(listOf<QuizData>()) }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        currentUser?.let {
            firestore.collection("users").document(it.uid).collection("quizzes")
                .get()
                .addOnSuccessListener { result ->
                    val quizList = result.documents.map { doc ->
                        doc.toObject<QuizData>() ?: QuizData()
                    }
                    quizzes = quizList
                }
                .addOnFailureListener { e ->
                    // Handle the error
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollStateGemini)
        ) {
            AppHeader("Automatic Quiz Maker")
            Text("Quizzes", modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.padding(8.dp))

            quizzes.forEach { quiz ->
                Button(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    onClick = { navController.navigate("QuizScreen/${quiz.response}") }
                ) {
                    Text(text = "${quiz.quizTitle} has ${quiz.questionCount} questions")
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Routes.PhotoScreenPage) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Make a new quiz.")
        }
    }
}

data class QuizData(
    val response: String = "",
    val quizTitle: String = "",
    val questionCount: String = ""
)
var firebaseSent = false

@Composable
fun PhotoScreenPage(navController: NavController){
    val scrollState= rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
    ) {
        PhotoPickerScreenGemini(
        "You will get the topic or notes in a picture analyze it and generate a quiz based on the amount of questions you get. " +
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
                "}"
        )
    }

}