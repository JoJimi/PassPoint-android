package com.example.passpoint.feature.question.ui

import com.example.passpoint.feature.question.data.dto.response.QuestionSearchResponse

/**
 * 질문 목록 화면의 상태.
 */
sealed interface QuestionListUiState {
    data object Loading : QuestionListUiState               // 최초 로딩 (첫 페이지)
    data class Error(val message: String) : QuestionListUiState

    data class Success(
        val questions: List<QuestionSearchResponse>,        // 지금까지 쌓인 목록
        val totalCount: Long = 0,                            // 전체 개수 (totalElements)
        val isLoadingMore: Boolean = false,                 // 다음 페이지 불러오는 중?
        val isLastPage: Boolean = false                     // 마지막 페이지 도달?
    ) : QuestionListUiState

}