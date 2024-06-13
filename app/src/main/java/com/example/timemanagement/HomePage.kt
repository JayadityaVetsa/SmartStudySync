package com.example.timemanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

        // Spacer to push the button to the middle of the screen
//        Spacer(modifier = Modifier.weight(1f))
//
//        // Button centered vertically
//        Button(
//            onClick = { navController.navigate(Routes.ChatPage) },
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text(text = "Chat")
//        }
//        Spacer(modifier = Modifier.weight(0.5f)) // Reduced weight for tighter spacing
//
//        Button(
//            onClick = { navController.navigate(Routes.HomeworkHelperPage) },
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text(text = "Homework Helper")
//        }
//
//        Spacer(modifier = Modifier.weight(0.5f)) // Reduced weight for tighter spacing
//
//        Button(
//            onClick = { navController.navigate(Routes.AutomaticQuizMakerPage) },
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text(text = "Automatic Quiz Maker")
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun HomePagePreview(){
    HomePage(navController = rememberNavController())
}