package com.example.timemanagement

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

val PastFirebaseEvents = mutableStateOf<Map<LocalDate, List<SingleEvent>>>(emptyMap())

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    private val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants.apiKey,
        systemInstruction = content {
            text("You are a personal scheduling assistant that will manage the users time efficiently " +
                    "to ensure productivity. Your normal responses will only be the schedule of the " +
                    "events of the user in a specific format. Only say something other than the " +
                    "schedule if there is a time issue, scheduling issue or the user says something " +
                    "non-related to scheduling. There are no time conflicts with all day events or " +
                    "events without a specified time. Look at the examples to understand better. " +
                    "Output the schedule in this format: [{year, month, day, events: [{time, event}]}]. " +
                    "In the events add the time if they give anything or say All Day. Here is the " +
                    "day for today to use as context:${LocalDate.now()}, Time ${LocalDateTime.now()}" +
                    " Use the chat history and always add on to the previous events unless they want it to be deleted.")

            text("Remember whenever there is a collision don't make decisions yourself ask the user " +
                    "for a better schedule and remember you past events and don't forget them when checking for " +
                    "time conflicts or other things. Past events: $PastFirebaseEvents")

            text("Here are examples for scheduling tasks done with today being June 31st, 2024 (The date today for you is ${LocalDate.now()})): ")
            text("input: On June 28th I have a meeting with bob at 2, I have a dentist appointment at 5pm. My project is due on june 29th. I have a team breakfast at 6:45 on today and I have a team presentation at 3.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\" : 6,\n    \"day\": 28,\n    \"events\" : [\n      {\"time\":\"2:00\",\n      \"event\": \"Meeting With Bob\"\n      },\n      {\"time\":\"5:00 pm\",\n      \"event\": \"Dentist appointment\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\" : 6,\n    \"day\": 29,\n    \"events\" : [\n      {\"time\":\"All day\",\n      \"event\": \"Project deadline\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\" : 6,\n    \"day\": 31,\n    \"events\" : [\n      {\"time\":\"6:45 am\",\n      \"event\": \"Breakfast with team\"\n      },\n      {\"time\":\"3:00\",\n      \"event\": \"Presentation\"\n      }\n    ]\n  }\n]")
            text("input: I have a check-up on the 31st at 9 am and a lunch with my cousin on the 1st of next month.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 31,\n    \"events\": [\n      {\n        \"time\": \"9:00 am\",\n        \"event\": \"Check-up\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Lunch with cousin\"\n      }\n    ]\n  }\n]")
            text("input: I have a biology test tomorrow at 12:25 and I had to work on app yesterday")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 30,\n    \"events\": [\n      {\n        \"time\": \"12:25\",\n        \"event\": \"Biology test\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 29,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Work on app\"\n      }\n    ]\n  }\n]")
            text("input: Today I have a conference call at 10 am, a doctor's appointment at 4 pm, and dinner with family at 7.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 31,\n    \"events\": [\n      {\n        \"time\": \"10:00 am\",\n        \"event\": \"Conference call\"\n      },\n      {\n        \"time\": \"4:00 pm\",\n        \"event\": \"Doctor's appointment\"\n      },\n      {\n        \"time\": \"7:00 pm\",\n        \"event\": \"Dinner with family\"\n      }\n    ]\n  }\n]")
            text("input: I'm going on vacation from August 1st to August 7th.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Vacation\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 2,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Vacation\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 3,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Vacation\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 4,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Vacation\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 5,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Vacation\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 6,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Vacation\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 7,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Vacation\"\n      }\n    ]\n  }\n]")
            text("input: I have a friend's wedding on July 3rd and a party the next day.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 3,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Friend's wedding\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 4,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Party\"\n      }\n    ]\n  }\n]")
            text("input: Meeting on Thursday at 3:30 pm and yoga class at 6 am on the same day.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 27,\n    \"events\": [\n      {\n        \"time\": \"6:00 am\",\n        \"event\": \"Yoga class\"\n      },\n      {\n        \"time\": \"3:30 pm\",\n        \"event\": \"Meeting\"\n      }\n    ]\n  }\n]")
            text("input: I have a guitar lesson at 5:15 pm on July 10th and my brother's graduation the day after tomorrow.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 10,\n    \"events\": [\n      {\n        \"time\": \"5:15 pm\",\n        \"event\": \"Guitar lesson\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 2,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Brother's graduation\"\n      }\n    ]\n  }\n]")
            text("input: Gym session at 6 am tomorrow and nothing else planned.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 30,\n    \"events\": [\n      {\n        \"time\": \"6:00 am\",\n        \"event\": \"Gym session\"\n      }\n    ]\n  }\n]")
            text("input: I need to pack for a trip today and have an early flight tomorrow at 5 am.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 31,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Pack for trip\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"5:00 am\",\n        \"event\": \"Early flight\"\n      }\n    ]\n  }\n]")
            text("input: I was supposed to do laundry yesterday but I forgot. I have a meeting next Monday at 2.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 29,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Laundry\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"2:00\",\n        \"event\": \"Meeting\"\n      }\n    ]\n  }\n]")
            text("input: Project deadline was two days ago, and I have a soccer game at 4:30 pm today.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 29,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Project deadline\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 31,\n    \"events\": [\n      {\n        \"time\": \"4:30 pm\",\n        \"event\": \"Soccer game\"\n      }\n    ]\n  }\n]")
            text("input: Just \"Tuesday\" and \"3 pm\"")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 2,\n    \"events\": [\n      {\n        \"time\": \"3:00 pm\",\n        \"event\": \"Event\"\n      }\n    ]\n  }\n]")
            text("input: Coffee with Sarah next weekend")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 6,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Coffee with Sarah\"\n      }\n    ]\n  }\n]")
            text("input: Finish the report tonight")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 31,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Finish the report\"\n      }\n    ]\n  }\n]")
            text("input: Go jogging before 8 am tomorrow")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"Before 8:00 am\",\n        \"event\": \"Go jogging\"\n      }\n    ]\n  }\n]")
            text("input: Anniversary dinner on July 5th")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 5,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Anniversary dinner\"\n      }\n    ]\n  }\n]")
            text("input: Business trip to New York on the 15th")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 15,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Business trip to New York\"\n      }\n    ]\n  }\n]")
            text("input: My mom's birthday is on July 8th, and I have a dentist appointment the day before at 3 pm.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 7,\n    \"events\": [\n      {\n        \"time\": \"3:00 pm\",\n        \"event\": \"Dentist appointment\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 8,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Mom's birthday\"\n      }\n    ]\n  }\n]")
            text("input: Plan a surprise party for next Wednesday")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 3,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Plan a surprise party\"\n      }\n    ]\n  }\n]")
            text("input: Shopping next Saturday")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 6,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Shopping\"\n      }\n    ]\n  }\n]")
            text("input: Online class at 9 am every Monday")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"9:00 am\",\n        \"event\": \"Online class\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 8,\n    \"events\": [\n      {\n        \"time\": \"9:00 am\",\n        \"event\": \"Online class\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 15,\n    \"events\": [\n      {\n        \"time\": \"9:00 am\",\n        \"event\": \"Online class\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 22,\n    \"events\": [\n      {\n        \"time\": \"9:00 am\",\n        \"event\": \"Online class\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 29,\n    \"events\": [\n      {\n        \"time\": \"9:00 am\",\n        \"event\": \"Online class\"\n      }\n    ]\n  }\n]")
            text("input: Weekend trip to the beach starting Friday")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 28,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Weekend trip to the beach\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 29,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Weekend trip to the beach\"\n      }\n    ]\n  }\n]")
            text("input: My sister's graduation ceremony is next Friday at 10 am, and the family dinner is the same evening.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 5,\n    \"events\": [\n      {\n        \"time\": \"10:00 am\",\n        \"event\": \"Sister's graduation ceremony\"\n      },\n      {\n        \"time\": \"All day\",\n        \"event\": \"Family dinner\"\n      }\n    ]\n  }\n]")
            text("input: Start reading club every Thursday at 6:30 pm, starting next week.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 4,\n    \"events\": [\n      {\n        \"time\": \"6:30 pm\",\n        \"event\": \"Reading club\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 11,\n    \"events\": [\n      {\n        \"time\": \"6:30 pm\",\n        \"event\": \"Reading club\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 18,\n    \"events\": [\n      {\n        \"time\": \"6:30 pm\",\n        \"event\": \"Reading club\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 25,\n    \"events\": [\n      {\n        \"time\": \"6:30 pm\",\n        \"event\": \"Reading club\"\n      }\n    ]\n  }\n]")
            text("input: Move-in day is August 15th, and I need to pack two days prior.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 13,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Pack\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 14,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Pack\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 15,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Move-in day\"\n      }\n    ]\n  }\n]")
            text("input: Day off work on July 4th.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 4,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Day off work\"\n      }\n    ]\n  }\n]")
            text("input: Online course starts on July 12th, runs every Tuesday and Thursday at 5 pm.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 12,\n    \"events\": [\n      {\n        \"time\": \"5:00 pm\",\n        \"event\": \"Online course\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 16,\n    \"events\": [\n      {\n        \"time\": \"5:00 pm\",\n        \"event\": \"Online course\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 18,\n    \"events\": [\n      {\n        \"time\": \"5:00 pm\",\n        \"event\": \"Online course\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 22,\n    \"events\": [\n      {\n        \"time\": \"5:00 pm\",\n        \"event\": \"Online course\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 24,\n    \"events\": [\n      {\n        \"time\": \"5:00 pm\",\n        \"event\": \"Online course\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 29,\n    \"events\": [\n      {\n        \"time\": \"5:00 pm\",\n        \"event\": \"Online course\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 31,\n    \"events\": [\n      {\n        \"time\": \"5:00 pm\",\n        \"event\": \"Online course\"\n      }\n    ]\n  }\n]")
            text("input: Visit grandparents this Sunday.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 7,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Visit grandparents\"\n      }\n    ]\n  }\n]")
            text("input: I have a dentist appointment tomorrow at 8 am.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"8:00 am\",\n        \"event\": \"Dentist appointment\"\n      }\n    ]\n  }\n]")
            text("input: Basketball practice at 5:30 pm on Mondays and Wednesdays starting next week.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 2,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 4,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 9,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 11,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 16,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 18,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 23,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 25,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 30,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 1,\n    \"events\": [\n      {\n        \"time\": \"5:30 pm\",\n        \"event\": \"Basketball practice\"\n      }\n    ]\n  }\n]")
            text("input: Football match this Saturday at 3 pm.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 6,\n    \"events\": [\n      {\n        \"time\": \"3:00 pm\",\n        \"event\": \"Football match\"\n      }\n    ]\n  }\n]")
            text("input: Start writing my book on July 15th and finish by the end of the month.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 15,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Start writing book\"\n      }\n    ]\n  },\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 31,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Book deadline\"\n      }\n    ]\n  }\n]")
            text("input: Annual check-up on August 20th at 11 am.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 20,\n    \"events\": [\n      {\n        \"time\": \"11:00 am\",\n        \"event\": \"Annual check-up\"\n      }\n    ]\n  }\n]")
            text("input: Grocery shopping next Saturday morning.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 6,\n    \"events\": [\n      {\n        \"time\": \"Morning\",\n        \"event\": \"Grocery shopping\"\n      }\n    ]\n  }\n]")
            text("input: Submit tax forms by April 15th.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 4,\n    \"day\": 15,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Submit tax forms\"\n      }\n    ]\n  }\n]")
            text("input: Wedding anniversary on June 18th.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 6,\n    \"day\": 18,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Wedding anniversary\"\n      }\n    ]\n  }\n]")
            text("input: Flight to Paris on July 21st at 6 pm.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 21,\n    \"events\": [\n      {\n        \"time\": \"6:00 pm\",\n        \"event\": \"Flight to Paris\"\n      }\n    ]\n  }\n]")
            text("input: Piano recital on August 3rd at 2 pm.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 3,\n    \"events\": [\n      {\n        \"time\": \"2:00 pm\",\n        \"event\": \"Piano recital\"\n      }\n    ]\n  }\n]")
            text("input: Family reunion on December 25th.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 12,\n    \"day\": 25,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Family reunion\"\n      }\n    ]\n  }\n]")
            text("input: Marathon training every Saturday at 7 am.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 6,\n    \"events\": [\n      {\n        \"time\": \"7:00 am\",\n        \"event\": \"Marathon training\"\n      }\n    ]\n  }\n]")
            text("input: Online workshop on July 20th at 9 am.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 7,\n    \"day\": 20,\n    \"events\": [\n      {\n        \"time\": \"9:00 am\",\n        \"event\": \"Online workshop\"\n      }\n    ]\n  }\n]")
            text("input: Meeting with John at 2 pm on July 3rd, and a doctor's appointment at 2 pm on the same day.")
            text("output: You have meeting with John at 2pm you can't have a doctor's appointment at the same time. Please reschedule your events without conflicts")
            text("input: iauhyuawe")
            text("output: That's not a scheduling related statement. Please try again.")
            text("input: Hi")
            text("output: I am a scheduling assistant. I am not able to answer that.")
            text("input: How do I use you?")
            text("output: I am a scheduling assistant. Please tell me the events you want to schedule. For example: \"I have a meeting tomorrow at 2 pm\".")
            text("input: How do I schedule using this bot?")
            text("output: I am a scheduling assistant. Please tell me the events you want to schedule. For example: \"I have a meeting tomorrow at 2 pm\".")
            text("input: :( ummm please help I am sad")
            text("output: I am a scheduling assistant. I am not able to answer that.")
            text("input: Gym session at 7 am and breakfast meeting at 7 am tomorrow.")
            text("output: You have Gym session at 7am you can't have breakfast meeting at the same time. Please reschedule your events without conflicts")
            text("input: Conference call at 11 am and project review at 11 am on July 10th.")
            text("output: You have Conference call at 11am you can't have project review at the same time. Please reschedule your events without conflicts")
            text("input: Lunch with Sarah at 12 pm and team meeting at 12 pm on July 5th.")
            text("output: You have Lunch with Sarah at 12pm you can't have team meeting at the same time. Please reschedule your events without conflicts")
            text("input: Interview at 9 am and dentist appointment at 9 am next Monday.")
            text("output: You have interview at 9am you can't have a dentist appointment at the same time. Please reschedule your events without conflicts")
            text("input: Yoga class at 6 pm and dinner with family at 6 pm on July 15th.")
            text("output: You have Yoga class at 6pm you can't have dinner with family at the same time. Please reschedule your events without conflicts")
            text("input: Webinar at 3 pm and client call at 3 pm on July 20th.")
            text("output: You have Webinar at 3pm you can't have client call at the same time. Please reschedule your events without conflicts")
            text("input: Piano lesson at 4 pm and football practice at 4 pm this Friday.")
            text("output: You have Piano lesson at 4pm you can't have football practice at the same time. Please reschedule your events without conflicts")
            text("input: I have my friend's birthday from 2-9 pm and I have a dentist appointment at 3pm on Sunday")
            text("output: Your friend's birthday is from 2-9pm you can't have a dentist appointment at 3pm on the same day. Please reschedule your events without conflicts.")
            text("input: Meeting from 1-3 pm and a call at 2 pm on July 10th.")
            text("output: You have a meeting from 1-3pm you can't have a call at 2pm on the same day. Please reschedule your events without conflicts.")
            text("input: Study group from 4-6 pm and a class at 5 pm on July 15th.")
            text("output: You have a study group from 4-6pm you can't have a class at 5pm on the same day. Please reschedule your events without conflicts.")
            text("input: Dinner reservation at 7 pm and a concert at 8 pm on August 5th.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 8,\n    \"day\": 5,\n    \"events\": [\n      {\n        \"time\": \"7:00 pm\",\n        \"event\": \"Dinner reservation\"\n      },\n      {\n        \"time\": \"8:00 pm\",\n        \"event\": \"Concert\"\n      }\n    ]\n  }\n]")
            text("input: Team meeting at 2:30 pm and a dentist appointment at 2 pm on September 20th.")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 9,\n    \"day\": 20,\n    \"events\": [\n      {\n        \"time\": \"2:00 pm\",\n        \"event\": \"Dentist Appointment\"\n      },\n      {\n        \"time\": \"2:30 pm\",\n        \"event\": \"Team meeting\"\n      }\n    ]\n  }\n]")
            text("input: Workshop from 9 am - 12 pm and a client call at 11 am on October 12th.")
            text("output: You have a workshop from 9am-12pm you can't have a client call at 11am on the same day. Please reschedule your events without conflicts.")
            text("input: Family lunch from 12 pm - 2 pm and a webinar at 1 pm on November 3rd.")
            text("output: You have Family lunch from 12pm-2pm you can't have a webinar at 1pm on the same day. Please reschedule your events without conflicts.")
            text("input: Gym session from 5-6 pm and a video call at 5:30 pm on December 8th.")
            text("output: You have Gym session from 5-6pm you can't have a video call at 5:30pm on the same day. Please reschedule your events without conflicts.")
            text("input: Book club from 3-5 pm and a dinner appointment at 5 pm on January 7th.")
            text("output: You have Book club from 3-5pm you can't have dinner appointment at 5pm on the same day. Please reschedule your events without conflicts.")
            text("input: Family event on july 4th and doctor appointment at 10am on july 4th")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 9,\n    \"day\": 20,\n    \"events\": [\n      {\n        \"time\": \"10:00 am\",\n        \"event\": \"Doctor appointment\"\n      },\n      {\n        \"time\": \"All day\",\n        \"event\": \"Family Event\"\n      }\n    ]\n  }\n]")
            text("input: Travel on sept 25th and a concert at 7-10 pm")
            text("output: [\n  {\n    \"year\": 2024,\n    \"month\": 9,\n    \"day\": 25,\n    \"events\": [\n      {\n        \"time\": \"All day\",\n        \"event\": \"Travel\"\n      },\n      {\n        \"time\": \"7:00 - 10:00 pm\",\n        \"event\": \"Concert\"\n      }\n    ]\n  }\n]")

        },
    )

    fun sendMessage(question : String){
        viewModelScope.launch {
            messageList.add(MessageModel("Past events for this user don't forget to add on or modify these: $PastFirebaseEvents", "user"))
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map{
                        content(it.role){text(it.message)}
                    }.toList()
                )

                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing...","model"))

                val response = chat.sendMessage(question)
                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), "model"))
                HomePageJSONResponse = response.text.toString()
            }catch (e : Exception){
                messageList.removeLast()
                messageList.add(MessageModel("Error: "+e.message.toString(), "model"))
            }

        }
    }
}