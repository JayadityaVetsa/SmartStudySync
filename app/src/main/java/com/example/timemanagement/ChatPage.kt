package com.example.timemanagement

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.timemanagement.ui.theme.Purple80
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

@Composable
fun ChatPage(navController: NavController, modifier : Modifier = Modifier, viewModel: ChatViewModel){

    Column(
        modifier = modifier
    ) {
        AppHeader("AI chatbot")
        MessageList(
            modifier = Modifier.weight(1f),
            viewModel.messageList
        )
        MessageInput(
            onMessageSend = {
                viewModel.sendMessage(it)
            }
        )
    }
}

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>){
    if (messageList.isEmpty()){
        Column (
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80
            )
            Text(
                text = "Hi, I am your personal scheduling assistant. Just paste your schedule " +
                        "for today to get started.",
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }else{
        LazyColumn (
            modifier=modifier,
            reverseLayout = true
        ){
            items(messageList.reversed()){
                MessageRow(messageModel = it)
            }
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel) {
    var eventsWorks by remember { mutableStateOf(false) }
    val isModel = messageModel.role == "model"
    val isUser = messageModel.role == "user"

    if (!messageModel.message.contains("Past events for this user don't forget to add on or modify these")) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                        .padding(
                            start = if (isModel) 8.dp else 60.dp,
                            end = if (isModel) 60.dp else 8.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                        .clip(RoundedCornerShape(48f))
                        .background(if (isModel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        .padding(16.dp)
                ) {
                    if (isModel) {
                        try {
                            val betterMessage = messageModel.message.substringAfter("```json\n")
                            val events = parseEvents(betterMessage.substringBefore("```"))
                            addToFirebase(events)
                            eventsWorks = true
                        } catch (e: Exception) {
                            eventsWorks = false
                            Text(
                                text = messageModel.message,
                                fontWeight = FontWeight.W500,
                                color = Color.White
                            )
                        }

                        if (eventsWorks) {
                            SelectionContainer {
                                Text(
                                    text = "Done! Please go to homepage and check your events",
                                    fontWeight = FontWeight.W500,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    else{
                        if(!messageModel.message.contains("Past events for this user don't forget to add on or modify these")){
                            SelectionContainer {
                                Text(
                                    text = messageModel.message,
                                    fontWeight = FontWeight.W500,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun addToFirebase(events: Map<LocalDate, List<String>>) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    val eventsStringMap = events.mapKeys { it.key.toStringFormat() }

    currentUser?.let { user ->
        val eventsCollection = firestore.collection("users").document(user.uid).collection("events")

        // Step 1: Delete all existing documents in the events collection
        eventsCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    eventsCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("Firebase", "Event deleted successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error deleting event", e)
                        }
                }

                // Step 2: Add the new events after deletion
                eventsStringMap.forEach { (date, events) ->
                    eventsCollection.add(Event(date, events))
                        .addOnSuccessListener {
                            Log.d("Firebase", "Event added successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error adding event", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error getting documents", e)
            }
    }
}


data class Event(
    val date: String = "", // Provide a default value
    val events: List<String> = emptyList() // Provide a default value
)


fun LocalDate.toStringFormat(): String {
    return this.toString() // Convert LocalDate to ISO-8601 string
}

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this) // Parse ISO-8601 string to LocalDate
}

@Composable
fun AppHeader(title: String = "") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = title,
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Composable
fun MessageInput(onMessageSend : (String)-> Unit) {
    var message by rememberSaveable {
        mutableStateOf("")
    }

    Row (
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            placeholder = {
                Text(text = "Type your message here")
            },
            onValueChange = {
                message = it
            }
        )
        IconButton(onClick = {
            if(message.isNotEmpty()){
                onMessageSend(message)
                message = ""
            }

        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}


