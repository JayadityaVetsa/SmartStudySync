package com.example.timemanagement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AutomaticQuizMakerPage (){
    val scrollStateGemini = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollStateGemini)
    ) {
        Text(text = "Automatic Quiz Helper")
        PhotoPickerScreenGemini()
    }
}

