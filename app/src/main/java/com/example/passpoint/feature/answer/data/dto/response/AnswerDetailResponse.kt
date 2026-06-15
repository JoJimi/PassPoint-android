package com.example.passpoint.feature.answer.data.dto.response

/**
 * GET /api/v1/answers/{id} 응답.
 * status != "DONE"이면 feedback은 null.
 */
data class AnswerDetailResponse(
    val answerId: Long,
    val question: QuestionInfo,
    val type: String,
    val answerText: String,
    val status: String,
    val feedback: FeedbackResponse?,
    val createdAt: String
)
