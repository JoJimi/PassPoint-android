package com.example.passpoint.feature.answer.ui

/**
 * 질문 상세/답변 화면의 "제출" 버튼 상태.
 */
sealed interface AnswerSubmitState {
    data object Idle : AnswerSubmitState
    data object Submitting : AnswerSubmitState
    data class Success(val answerId: Long) : AnswerSubmitState
    data class Error(val message: String) : AnswerSubmitState
}
