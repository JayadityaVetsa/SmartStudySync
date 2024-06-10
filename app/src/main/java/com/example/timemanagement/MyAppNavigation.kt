package com.example.timemanagement

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MyAppNavigation(viewModel: ChatViewModel){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HomePage, builder = {
        composable(Routes.HomePage){
            HomePage(navController)
        }
        composable(Routes.ChatPage){
            ChatPage(navController, viewModel = viewModel)
        }
        composable(Routes.HomeworkHelperPage){
            HomeworkHelperPage()
        }
        composable(Routes.AutomaticQuizMakerPage){
            AutomaticQuizMakerPage()
        }
    })
}