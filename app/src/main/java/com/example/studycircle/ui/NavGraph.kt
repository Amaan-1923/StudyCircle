package com.example.studycircle.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studycircle.domain.model.ChatRoom
import com.example.studycircle.ui.ai.AiScreen
import com.example.studycircle.ui.auth.AuthViewModel
import com.example.studycircle.ui.auth.LoginScreen
import com.example.studycircle.ui.auth.RegisterScreen
import com.example.studycircle.ui.chat.ChatListScreen
import com.example.studycircle.ui.chat.ChatRoomScreen
import com.example.studycircle.ui.chat.ChatViewModel
import com.example.studycircle.ui.components.BottomNavBar
import com.example.studycircle.ui.feed.CreatePostScreen
import com.example.studycircle.ui.feed.FeedScreen
import com.example.studycircle.ui.map.MapScreen
import com.example.studycircle.ui.splash.SplashScreen
import com.example.studycircle.ui.stats.StatsScreen
import com.google.firebase.auth.FirebaseAuth
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_POST = "create_post"
    const val AI_ASSISTANT = "ai_assistant"
    const val CHAT = "chat"
    const val CHAT_ROOM = "chat_room/{roomId}/{roomName}/{roomEmoji}/{roomDescription}"
    const val MAP = "map"
    const val STATS = "stats"
}

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
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
            }
        ) {
            // Splash Screen
            composable(
                route = Routes.SPLASH,
                enterTransition = { fadeIn(animationSpec = tween(500)) },
                exitTransition = { fadeOut(animationSpec = tween(500)) }
            ) {
                SplashScreen(
                    onSplashFinished = {
                        val destination = if (authViewModel.isLoggedIn())
                            Routes.HOME else Routes.LOGIN
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

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
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userName = currentUser?.displayName
                    ?: currentUser?.email?.substringBefore("@")
                    ?: "Student"

                FeedScreen(
                    userName = userName,
                    onCreatePost = {
                        navController.navigate(Routes.CREATE_POST)
                    },
                    onAiAssistantClick = {
                        navController.navigate(Routes.AI_ASSISTANT)
                    },
                    onProfileClick = {
                        navController.navigate(Routes.STATS)
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

            // Chat List Screen
            composable(Routes.CHAT) {
                val chatViewModel: ChatViewModel = viewModel()
                ChatListScreen(
                    onRoomClick = { room ->
                        val encodedName = URLEncoder.encode(room.name, "UTF-8")
                        val encodedEmoji = URLEncoder.encode(room.emoji, "UTF-8")
                        val encodedDesc = URLEncoder.encode(room.description, "UTF-8")
                        navController.navigate(
                            "chat_room/${room.id}/$encodedName/$encodedEmoji/$encodedDesc"
                        )
                    },
                    viewModel = chatViewModel
                )
            }

            // Chat Room Screen
            composable(
                route = Routes.CHAT_ROOM,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.StringType },
                    navArgument("roomName") { type = NavType.StringType },
                    navArgument("roomEmoji") { type = NavType.StringType },
                    navArgument("roomDescription") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                val roomName = URLDecoder.decode(
                    backStackEntry.arguments?.getString("roomName") ?: "", "UTF-8"
                )
                val roomEmoji = URLDecoder.decode(
                    backStackEntry.arguments?.getString("roomEmoji") ?: "💬", "UTF-8"
                )
                val roomDescription = URLDecoder.decode(
                    backStackEntry.arguments?.getString("roomDescription") ?: "", "UTF-8"
                )
                val chatViewModel: ChatViewModel = viewModel()
                ChatRoomScreen(
                    room = ChatRoom(
                        id = roomId,
                        name = roomName,
                        emoji = roomEmoji,
                        description = roomDescription
                    ),
                    onBack = { navController.popBackStack() },
                    viewModel = chatViewModel
                )
            }

            // Map Screen
            composable(Routes.MAP) {
                MapScreen()
            }

            // Stats Screen
            composable(Routes.STATS) {
                StatsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}