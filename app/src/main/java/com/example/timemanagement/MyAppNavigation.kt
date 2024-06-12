package com.example.timemanagement

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthScreenSetup(viewModel: ChatViewModel){
    var isLoggedIn by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        MyAppNavigation(viewModel)
    } else {
        var showSignUpScreen by remember { mutableStateOf(true) }

        if (showSignUpScreen) {
            SignUpScreen(
                onSignUp = { email, password ->
                    signUp(email, password,
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
                onLogin = { email, password ->
                    login(email, password,
                        onSuccess = { isLoggedIn = true },
                        onFailure = { e ->
                            // Handle error
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