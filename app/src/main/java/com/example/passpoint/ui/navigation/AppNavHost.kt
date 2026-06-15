package com.example.passpoint.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.answer.ui.FeedbackScreen
import com.example.passpoint.feature.answer.ui.ProcessingScreen
import com.example.passpoint.feature.auth.ui.LoginScreen
import com.example.passpoint.feature.home.ui.HomeScreen
import com.example.passpoint.feature.question.ui.QuestionDetailScreen
import com.example.passpoint.feature.question.ui.QuestionListScreen

@Composable
fun AppNavHost() {
    // 운전대: 화면 이동을 명령하는 객체
    val navController = rememberNavController()

    // 토큰 재발급도 실패해서 세션이 끊겼을 때, 어느 화면에 있었든 로그인 화면으로 보낸다.
    LaunchedEffect(Unit) {
        RetrofitClient.sessionExpired.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    // 액자: 현재 주소에 맞는 화면을 이 자리에 그린다.
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN // 앱 시작 시 첫 화면
    ) {
        // 로그인 화면
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // 성공하면 홈으로 이동하면서, 로그인 화면은 스택에서 제거
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 홈 화면
        composable(Routes.HOME) {
            HomeScreen(
                onGoToQuestions = {
                    navController.navigate(Routes.QUESTION_LIST)
                }
            )
        }

        // 질문 목록 화면
        composable(Routes.QUESTION_LIST) {
            QuestionListScreen(
                onQuestionClick = { id ->
                    navController.navigate(Routes.questionDetail(id))
                }
            )
        }

        // 질문 상세 화면 (주소에 {id}를 받는다)
        composable(
            route = Routes.QUESTION_DETAIL,
            arguments = listOf(navArgument("id") {type = NavType.LongType})
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

        // 처리 중(분석 중) 화면 (주소에 {id}로 answerId를 받는다)
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

        // 피드백 결과 화면 (주소에 {id}로 answerId를 받는다)
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
                    navController.navigate(Routes.QUESTION_LIST) {
                        popUpTo(Routes.QUESTION_LIST) { inclusive = true }
                    }
                }
            )
        }
    }
}