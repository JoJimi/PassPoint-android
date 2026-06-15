package com.example.passpoint.feature.answer.ui

import com.example.passpoint.feature.answer.data.dto.response.AnswerDetailResponse

/**
 * 피드백 결과 화면의 상태.
 */
sealed interface FeedbackUiState {
    data object Loading : FeedbackUiState
    data class Error(val message: String) : FeedbackUiState
    data class Success(val answer: AnswerDetailResponse) : FeedbackUiState
}
