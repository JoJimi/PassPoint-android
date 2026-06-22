package com.example.passpoint.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
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
import com.example.passpoint.feature.auth.ui.EmailLoginScreen
import com.example.passpoint.feature.auth.ui.EmailSignupScreen
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

private fun navigateToTab(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun tabIndexOf(route: String?): Int = bottomNavItems.indexOfFirst { it.route == route }

// 하단 탭끼리 이동할 때 탭 순서에 맞춰 옆으로 슬라이드되도록 방향을 계산한다.
// 탭이 아닌 화면으로 이동할 때는 그냥 페이드 처리한다.
private val tabEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val from = tabIndexOf(initialState.destination.route)
    val to = tabIndexOf(targetState.destination.route)
    when {
        from == -1 || to == -1 || from == to -> fadeIn()
        to > from -> slideInHorizontally(initialOffsetX = { it })
        else -> slideInHorizontally(initialOffsetX = { -it })
    }
}

private val tabExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val from = tabIndexOf(initialState.destination.route)
    val to = tabIndexOf(targetState.destination.route)
    when {
        from == -1 || to == -1 || from == to -> fadeOut()
        to > from -> slideOutHorizontally(targetOffsetX = { -it })
        else -> slideOutHorizontally(targetOffsetX = { it })
    }
}

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
            modifier = Modifier
                .padding(paddingValues)
                .let { base ->
                    if (currentRoute !in bottomNavRoutes) return@let base
                    base.pointerInput(currentRoute) {
                        val currentIndex = tabIndexOf(currentRoute)
                        var dragAmount = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragAmount = 0f },
                            onHorizontalDrag = { change, delta ->
                                dragAmount += delta
                                change.consume()
                            },
                            onDragEnd = {
                                val threshold = size.width / 4f
                                when {
                                    dragAmount <= -threshold && currentIndex < bottomNavItems.lastIndex ->
                                        navigateToTab(navController, bottomNavItems[currentIndex + 1].route)
                                    dragAmount >= threshold && currentIndex > 0 ->
                                        navigateToTab(navController, bottomNavItems[currentIndex - 1].route)
                                }
                            }
                        )
                    }
                }
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToEmailLogin = {
                        navController.navigate(Routes.EMAIL_LOGIN)
                    }
                )
            }

            composable(Routes.EMAIL_LOGIN) {
                EmailLoginScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToSignup = { navController.navigate(Routes.EMAIL_SIGNUP) },
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.EMAIL_SIGNUP) {
                EmailSignupScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignupSuccess = {
                        // 가입 직후 자동 로그인하지 않고, 가입 화면 진입 전 이메일 로그인 화면으로 돌려보내 재로그인을 받는다.
                        navController.popBackStack()
                    }
                )
            }

            composable(
                Routes.HOME,
                enterTransition = tabEnterTransition,
                exitTransition = tabExitTransition,
                popEnterTransition = tabEnterTransition,
                popExitTransition = tabExitTransition
            ) {
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

            composable(
                Routes.QUESTION_LIST,
                enterTransition = tabEnterTransition,
                exitTransition = tabExitTransition,
                popEnterTransition = tabEnterTransition,
                popExitTransition = tabExitTransition
            ) {
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
            composable(
                Routes.LEARNING_LOG,
                enterTransition = tabEnterTransition,
                exitTransition = tabExitTransition,
                popEnterTransition = tabEnterTransition,
                popExitTransition = tabExitTransition
            ) {
                LearningLogScreen(
                    onAnswerClick = { answerId ->
                        navController.navigate(Routes.answerFeedback(answerId))
                    }
                )
            }

            // F11: 마이페이지
            composable(
                Routes.MY_PAGE,
                enterTransition = tabEnterTransition,
                exitTransition = tabExitTransition,
                popEnterTransition = tabEnterTransition,
                popExitTransition = tabExitTransition
            ) {
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
                        navigateToTab(navController, item.route)
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
