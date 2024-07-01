package com.example.timemanagement

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.timemanagement.ui.theme.*


@Composable
fun AuthScreenSetup(viewModel: ChatViewModel) {
    var isLoggedIn by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        MyAppNavigation(viewModel)
    } else {
        var showSignUpScreen by remember { mutableStateOf(false) } // Start with LoginScreen

        if (showSignUpScreen) {
            SignUpScreen(
                onSignUp = { name, email, password ->
                    signUp(name, email, password,
                        onSuccess = { isLoggedIn = true },
                        onFailure = { e ->
                            // Handle error
                        }
                    )
                },
                onNavigateToLogin = { showSignUpScreen = false }
            )
        } else {
            LoginScreen(
                onLogin = { email, password, onError ->
                    login(email, password,
                        onSuccess = { isLoggedIn = true },
                        onFailure = { errorMessage ->
                            onError(errorMessage) // Pass the error message to the composable
                        }
                    )
                },
                onNavigateToSignUp = { showSignUpScreen = true }
            )
        }
    }
}


@Composable
fun MyAppNavigation(viewModel: ChatViewModel){
    val navController = rememberNavController()
    val selected = remember {
        mutableStateOf(0)
    }

    Scaffold (
        bottomBar = {
            BottomAppBar(
                containerColor = color,
            ){

                IconButton(
                    onClick = {
                        selected.value = 1
                        navController.navigate(Routes.ChatPage){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(
                        if (selected.value == 1) painterResource(R.drawable.baseline_question_answer_24) else painterResource(R.drawable.outline_question_answer_24),
                        contentDescription = "Chat",
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = {
                        selected.value = 2
                        navController.navigate(Routes.HomeworkHelperPage){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(
                        if (selected.value == 0) painterResource(R.drawable.baseline_check_24) else painterResource(R.drawable.outline_check_24),
                        contentDescription = "Homework Helper",
                        modifier = Modifier.size(26.dp),

                    )
                }

                IconButton(
                    onClick = {
                        selected.value = 0
                        navController.navigate(Routes.HomePage){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(
                        if (selected.value == 0) painterResource(R.drawable.baseline_home_24) else painterResource(R.drawable.outline_home_24),
                        contentDescription = "Home",
                        modifier = Modifier.size(26.dp),

                    )
                }

                IconButton(
                    onClick = {
                        selected.value = 3
                        navController.navigate(Routes.AutomaticQuizMakerPage){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(
                        if (selected.value == 3) painterResource(R.drawable.baseline_quiz_24) else painterResource(R.drawable.outline_quiz_24),
                        contentDescription = "Quiz Maker",
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = {
                        selected.value = 4
                        navController.navigate(Routes.SettingsPage){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(
                        painterResource(R.drawable.baseline_settings_24),
                        contentDescription = "Settings",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ){ paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.HomePage,
            modifier = Modifier.padding(paddingValues),
            builder = {
                composable(Routes.HomePage) {
                    HomePage(navController)
                }
                composable(Routes.ChatPage) {
                    ChatPage(navController, viewModel = viewModel)
                }
                composable(Routes.HomeworkHelperPage) {
                    HomeworkHelperPage(navController)
                }
                composable(Routes.AutomaticQuizMakerPage) {
                    AutomaticQuizMakerPage(navController)
                }
                composable(Routes.PhotoScreenPage) {
                    PhotoScreenPage(navController)
                }
                composable(Routes.SettingsPage) {
                    SettingsPage()
                }
                composable(route = "QuizScreen/{response}", arguments = listOf(navArgument("response") { type = NavType.StringType })) {
                    val response = it.arguments?.getString("response") ?: ""
                    val quiz = parseQuiz(response)
                    QuizScreen(quiz, navController)
                }
            }
        )
    }
}