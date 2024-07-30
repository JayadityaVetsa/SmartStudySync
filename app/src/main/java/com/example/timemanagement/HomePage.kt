package com.example.timemanagement

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

var HomePageJSONResponse = ""


@Composable
fun HomePage(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var events by remember { mutableStateOf<Map<LocalDate, List<String>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) } // Track loading state

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            firestore.collection("users").document(user.uid).collection("events")
                .get()
                .addOnSuccessListener { result ->
                    val fetchedEvents = result.documents.mapNotNull { doc ->
                        doc.toObject<Event>()?.let { event ->
                            if (event.date.isNotBlank() && event.events.isNotEmpty()) {
                                event.date.toLocalDate() to event.events
                            } else {
                                null
                            }
                        }
                    }.toMap()

                    events = fetchedEvents
                    PastFirebaseEvents.value = fetchedEvents
                    isLoading = false // Update loading state
                    Log.e("Firebase", "Events added")
                }
                .addOnFailureListener { e ->
                    isLoading = false // Update loading state even on failure
                    Log.e("Firebase", "Error fetching events", e)
                }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Home Page",
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )

            if (isLoading) {
                // Center the loading indicator and text using a Box with contentAlignment
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp), // Optional padding if needed
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally // Center content in column
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading...",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 8.dp) // Optional padding
                        )
                    }
                }
            } else {
                // Show calendar when not loading
                CalendarApp(events)
                Log.e("Firebase", "Calendar ran $events")
                Log.e("Firebase", "Past events $PastFirebaseEvents")
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Routes.ChatPage) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Make a new event using AI.")
        }
    }
}

fun parseEvents(jsonString: String): Map<LocalDate, List<String>> {
    val json = Json { ignoreUnknownKeys = true } // Configure the JSON parser
    val eventDays = json.decodeFromString<List<EventDay>>(jsonString)

    val eventsMap = mutableMapOf<LocalDate, List<String>>()

    for (eventDay in eventDays) {
        val date = LocalDate.of(eventDay.year, eventDay.month, eventDay.day)
        eventsMap[date] = eventDay.events
    }

    return eventsMap
}

@Serializable
data class EventDay(
    val year: Int,
    val month: Int,
    val day: Int,
    val events: List<String>
)

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun HomePagePreview(){
    HomePage(navController = rememberNavController())
}