package com.example.passpoint.feature.answer.ui

/**
 * 처리 중(분석 중) 화면의 상태.
 * 2주차는 동기 처리라 첫 폴링에서 바로 Done/Failed로 떨어진다.
 */
sealed interface ProcessingUiState {
    data object Loading : ProcessingUiState
    data class Done(val answerId: Long) : ProcessingUiState
    data object Failed : ProcessingUiState
    data object Timeout : ProcessingUiState
}
