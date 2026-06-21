package com.example.studycircle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studycircle.ui.ai.AiScreen
import com.example.studycircle.ui.auth.AuthViewModel
import com.example.studycircle.ui.auth.LoginScreen
import com.example.studycircle.ui.auth.RegisterScreen
import com.example.studycircle.ui.components.BottomNavBar
import com.example.studycircle.ui.feed.CreatePostScreen
import com.example.studycircle.ui.feed.FeedScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_POST = "create_post"
    const val AI_ASSISTANT = "ai_assistant"
    const val CHAT = "chat"
    const val MAP = "map"
}

// Routes that should show the bottom nav bar
val bottomNavRoutes = listOf(
    Routes.HOME,
    Routes.AI_ASSISTANT,
    Routes.CHAT,
    Routes.MAP
)

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val startDestination = if (authViewModel.isLoggedIn()) Routes.HOME else Routes.LOGIN
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login Screen
            composable(Routes.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    }
                )
            }

            // Register Screen
            composable(Routes.REGISTER) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Home / Feed Screen
            composable(Routes.HOME) {
                FeedScreen(
                    userName = "Student",
                    onCreatePost = {
                        navController.navigate(Routes.CREATE_POST)
                    },
                    onAiAssistantClick = {
                        navController.navigate(Routes.AI_ASSISTANT)
                    }
                )
            }

            // Create Post Screen
            composable(Routes.CREATE_POST) {
                CreatePostScreen(
                    onPostCreated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // AI Assistant Screen
            composable(Routes.AI_ASSISTANT) {
                AiScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Chat Screen (placeholder)
            composable(Routes.CHAT) {
                ChatPlaceholder()
            }

            // Map Screen (placeholder)
            composable(Routes.MAP) {
                MapPlaceholder()
            }
        }
    }
}

@Composable
fun ChatPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "💬 Chat\nComing soon...",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🗺️ Nearby Students\nComing soon...",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}