package com.example.passpoint.feature.home.ui

import com.example.passpoint.feature.answer.data.dto.response.AnswerSummaryResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Success(
        val nickname: String,
        val stats: UserStatsResponse,
        val recentAnswers: List<AnswerSummaryResponse>
    ) : HomeUiState
}
