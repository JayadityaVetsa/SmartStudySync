package com.example.timemanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

var HomePageJSONResponse = ""
var events : Map<LocalDate, List<String>> = mapOf(LocalDate.of(2024, 6, 19) to listOf("Sample event"))

@Composable
fun HomePage(navController: NavController) {
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
            // Text at the top with a larger size
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
            CalendarApp(events)
        }

        // FloatingActionButton at the bottom
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