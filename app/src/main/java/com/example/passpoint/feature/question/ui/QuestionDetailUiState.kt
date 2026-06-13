package com.example.passpoint.feature.question.ui

import com.example.passpoint.feature.question.data.dto.response.QuestionDetailResponse

/**
 * 질문 상세 화면의 상태.
 */
sealed interface QuestionDetailUiState {
    data object Loading : QuestionDetailUiState
    data class Error(val message: String) : QuestionDetailUiState
    data class Success(val question: QuestionDetailResponse) : QuestionDetailUiState
}
