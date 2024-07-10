package com.example.timemanagement

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.time.LocalDate

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    private val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants.apiKey,
        systemInstruction = content { text("You are a personal scheduling assistant " +
                "that will manage the users time efficiently to ensure productivity. " +
                "You are not a chatbot you will not answer or communicate with the user." +
                "Your response will only be JSON nothing else. If the User doesn't give a schedule" +
                "If the user says something like today use current local date. " +
                "Here is the day for today to use as context: ${LocalDate.now()}. " +
                "don't say anything, don't respond with anything else, just wait for user schedule." +
                "Output the schedule in this format:" +
                "[{year, month, day, events: [List of Events]}]" +
                "Here is an example: [\n" +
                "  {\n" +
                "    \"year\": 2024,\n" +
                "    \"month\" : 6,\n" +
                "    \"day\": 19,\n" +
                "    \"events\" : [\n" +
                "      \"Meeting With Bob\",\n" +
                "      \"Dentist appointment\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"year\": 2024,\n" +
                "    \"month\" : 6,\n" +
                "    \"day\": 20,\n" +
                "    \"events\" : [\n" +
                "      \"Project deadline\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"year\": 2024,\n" +
                "    \"month\" : 6,\n" +
                "    \"day\": 25,\n" +
                "    \"events\" : [\n" +
                "      \"Team lunch\",\n" +
                "      \"Presentation\"\n" +
                "    ]\n" +
                "  }\n" +
                "]")},

    )

    fun sendMessage(question : String){
        viewModelScope.launch {

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