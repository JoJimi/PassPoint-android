package com.example.passpoint.feature.answer.ui

import com.example.passpoint.feature.answer.data.dto.response.AnswerSummaryResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

sealed interface LearningLogUiState {
    data object Loading : LearningLogUiState
    data class Error(val message: String) : LearningLogUiState
    data class Success(
        val answers: List<AnswerSummaryResponse>,
        val stats: UserStatsResponse?,
        val isLoadingMore: Boolean = false,
        val isLastPage: Boolean = false
    ) : LearningLogUiState
}
