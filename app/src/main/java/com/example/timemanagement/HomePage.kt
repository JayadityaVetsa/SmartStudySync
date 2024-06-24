package com.example.timemanagement

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun HomePage(navController: NavController){
    Column(
        Modifier
            .fillMaxSize()
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
        if (HomePageJSONResponse.isEmpty()){
            HomePageJSONResponse = """
            [
              {
                "year": 2024,
                "month": 6,
                "day": 19,
                "events": [
                  "Meeting With Bob",
                  "Dentist appointment"
                ]
              },
              {
                "year": 2024,
                "month": 6,
                "day": 20,
                "events": [
                  "Project deadline"
                ]
              },
              {
                "year": 2024,
                "month": 6,
                "day": 25,
                "events": [
                  "Team lunch",
                  "Presentation"
                ]
              }
            ]
            """
        }
        val events: Map<LocalDate, List<String>> = parseEvents(HomePageJSONResponse)
//        Log.d("HomePage", "Events: $events")
        CalendarApp(events)
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