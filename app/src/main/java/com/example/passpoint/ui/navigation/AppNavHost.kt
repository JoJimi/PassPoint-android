package com.example.passpoint.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.answer.ui.FeedbackScreen
import com.example.passpoint.feature.answer.ui.LearningLogScreen
import com.example.passpoint.feature.answer.ui.ProcessingScreen
import com.example.passpoint.feature.auth.ui.LoginScreen
import com.example.passpoint.feature.bookmark.ui.BookmarkListScreen
import com.example.passpoint.feature.home.ui.HomeScreen
import com.example.passpoint.feature.question.ui.QuestionDetailScreen
import com.example.passpoint.feature.question.ui.QuestionListScreen
import com.example.passpoint.feature.user.ui.MyPageScreen

private val PassPurple = Color(0xFF5B4FE8)

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "홈", Icons.Filled.Home),
    BottomNavItem(Routes.QUESTION_LIST, "질문 탐색", Icons.Filled.Search),
    BottomNavItem(Routes.LEARNING_LOG, "학습 기록", Icons.Filled.List),
    BottomNavItem(Routes.MY_PAGE, "마이페이지", Icons.Filled.Person)
)

private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        RetrofitClient.sessionExpired.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    navController = navController
                )
            }
        },
        containerColor = Color(0xFFF7F7FA)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onGoToQuestions = {
                        navController.navigate(Routes.QUESTION_LIST)
                    },
                    onSeeAllAnswers = {
                        navController.navigate(Routes.LEARNING_LOG) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAnswerClick = { answerId ->
                        navController.navigate(Routes.answerFeedback(answerId))
                    },
                    onCategoryClick = { category ->
                        navController.navigate(Routes.categoryQuestions(category))
                    }
                )
            }

            composable(Routes.QUESTION_LIST) {
                QuestionListScreen(
                    onQuestionClick = { id ->
                        navController.navigate(Routes.questionDetail(id))
                    }
                )
            }

            // 홈의 카테고리 칩에서 들어가는 화면. 하단 탭이 아니라 뒤로가기로 나가는 화면이라 별도 라우트로 둔다.
            composable(
                route = Routes.CATEGORY_QUESTIONS,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: return@composable
                QuestionListScreen(
                    initialCategoryValue = category,
                    onBack = { navController.popBackStack() },
                    onQuestionClick = { id ->
                        navController.navigate(Routes.questionDetail(id))
                    }
                )
            }

            composable(
                route = Routes.QUESTION_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                QuestionDetailScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                    onSubmitSuccess = { answerId ->
                        navController.navigate(Routes.answerProcessing(answerId))
                    }
                )
            }

            composable(
                route = Routes.ANSWER_PROCESSING,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val answerId = backStackEntry.arguments?.getLong("id") ?: return@composable
                ProcessingScreen(
                    answerId = answerId,
                    onDone = { id ->
                        navController.navigate(Routes.answerFeedback(id)) {
                            popUpTo(Routes.QUESTION_DETAIL) { inclusive = false }
                        }
                    },
                    onRetry = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.ANSWER_FEEDBACK,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val answerId = backStackEntry.arguments?.getLong("id") ?: return@composable
                FeedbackScreen(
                    answerId = answerId,
                    onBack = { navController.popBackStack() },
                    onRetryQuestion = { questionId ->
                        navController.navigate(Routes.questionDetail(questionId)) {
                            popUpTo(Routes.QUESTION_DETAIL) { inclusive = true }
                        }
                    },
                    onGoToList = {
                        navController.navigate(Routes.LEARNING_LOG) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                )
            }

            // F10: 학습 기록 화면
            composable(Routes.LEARNING_LOG) {
                LearningLogScreen(
                    onAnswerClick = { answerId ->
                        navController.navigate(Routes.answerFeedback(answerId))
                    }
                )
            }

            // F11: 마이페이지
            composable(Routes.MY_PAGE) {
                MyPageScreen(
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onBookmarkClick = {
                        navController.navigate(Routes.BOOKMARK_LIST)
                    }
                )
            }

            // F12: 북마크 목록
            composable(Routes.BOOKMARK_LIST) {
                BookmarkListScreen(
                    onBack = { navController.popBackStack() },
                    onQuestionClick = { id ->
                        navController.navigate(Routes.questionDetail(id))
                    }
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(currentRoute: String?, navController: NavController) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PassPurple,
                    selectedTextColor = PassPurple,
                    indicatorColor = Color(0xFFEEECFB),
                    unselectedIconColor = Color(0xFF8E8E9A),
                    unselectedTextColor = Color(0xFF8E8E9A)
                )
            )
        }
    }
}
