package com.example.timemanagement

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun AutomaticQuizMakerPage(navController: NavController) {
    val scrollStateGemini = rememberScrollState()
    var quizzes by remember { mutableStateOf(listOf<QuizData>()) }
    var isLoading by remember { mutableStateOf(true) } // State to track loading
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        currentUser?.let {
            firestore.collection("users").document(it.uid).collection("quizzes")
                .get()
                .addOnSuccessListener { result ->
                    val quizList = result.documents.map { doc ->
                        doc.toObject<QuizData>()?.apply { documentId = doc.id } ?: QuizData()
                    }
                    quizzes = quizList
                    isLoading = false // Set loading to false once data is fetched
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error fetching quizzes", e)
                    isLoading = false // Set loading to false even on failure
                }
        } ?: run {
            isLoading = false // Set loading to false if no user is logged in
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            // Show a loading indicator with "Loading..." text
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...")
            }
        } else {
            // Display the quiz list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollStateGemini)
            ) {
                AppHeader("Automatic Quiz Maker")
                Spacer(modifier = Modifier.padding(8.dp))

                quizzes.forEach { quiz ->
                    SwipeToDismissQuizItem(quiz, navController, firestore, currentUser)
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


@Composable
fun SwipeToDismissQuizItem(quiz: QuizData, navController: NavController, firestore: FirebaseFirestore, user: FirebaseUser?) {
    var isDismissed by remember { mutableStateOf(false) }
    if (!isDismissed) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { navController.navigate("QuizScreen/${quiz.response}") }
                .swipeToDismiss {
                    isDismissed = true
                    user?.let {
                        firestore.collection("users").document(user.uid).collection("quizzes").document(quiz.documentId)
                            .delete()
                            .addOnSuccessListener { Log.d("Firebase", "Quiz successfully deleted") }
                            .addOnFailureListener { e -> Log.e("Firebase", "Error deleting quiz", e) }
                    }
                } // Make the box clickable
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = quiz.quizTitle,
                        style = MaterialTheme.typography.headlineSmall // Adjust the style as needed
                    )
                    Text(
                        text = if (quiz.questionCount == "1") "${quiz.questionCount} question" else "${quiz.questionCount} questions",
                        style = MaterialTheme.typography.bodyMedium  // Adjust the style as needed
                    )
                }
                IconButton(onClick = {
                    isDismissed = true
                    user?.let {
                        firestore.collection("users").document(user.uid).collection("quizzes").document(quiz.documentId)
                            .delete()
                            .addOnSuccessListener { Log.d("Firebase", "Quiz successfully deleted") }
                            .addOnFailureListener { e -> Log.e("Firebase", "Error deleting quiz", e) }
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Quiz")
                }
            }
        }
    }
}

fun Modifier.swipeToDismiss(
    onDismissed: () -> Unit
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    pointerInput(Unit) {
        // Used to calculate fling decay.
        val decay = splineBasedDecay<Float>(this)
        // Use suspend functions for touch events and the Animatable.
        coroutineScope {
            while (true) {
                val velocityTracker = VelocityTracker()
                // Stop any ongoing animation.
                offsetX.stop()
                awaitPointerEventScope {
                    // Detect a touch down event.
                    val pointerId = awaitFirstDown().id

                    horizontalDrag(pointerId) { change ->
                        // Update the animation value with touch events.
                        launch {
                            offsetX.snapTo(
                                offsetX.value + change.positionChange().x
                            )
                        }
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                    }
                }
                // No longer receiving touch events. Prepare the animation.
                val velocity = velocityTracker.calculateVelocity().x
                val targetOffsetX = decay.calculateTargetValue(
                    offsetX.value,
                    velocity
                )
                // The animation stops when it reaches the bounds.
                offsetX.updateBounds(
                    lowerBound = -size.width.toFloat(),
                    upperBound = size.width.toFloat()
                )
                launch {
                    if (targetOffsetX.absoluteValue <= size.width) {
                        // Not enough velocity; Slide back.
                        offsetX.animateTo(
                            targetValue = 0f,
                            initialVelocity = velocity
                        )
                    } else {
                        // The element was swiped away.
                        offsetX.animateDecay(velocity, decay)
                        onDismissed()
                    }
                }
            }
        }
    }
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
}

data class QuizData(
    var documentId: String = "",
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
        PhotoPickerScreenGeminiQuiz(
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