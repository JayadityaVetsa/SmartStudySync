package com.example.timemanagement

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    private val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants.apiKey,
        systemInstruction = content { text("You are a personal scheduling assistant " +
                "that will manage the users time efficiently to ensure productivity. " +
                "Don't answer other non-related questions except for " +
                "questions about scheduling and time management. " +
                "Say 'I can only answer questions about scheduling and time management' " +
                "if faced with non-related questions") },
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
            }catch (e : Exception){
                messageList.removeLast()
                messageList.add(MessageModel("Error: "+e.message.toString(), "model"))
            }


        }
    }
}