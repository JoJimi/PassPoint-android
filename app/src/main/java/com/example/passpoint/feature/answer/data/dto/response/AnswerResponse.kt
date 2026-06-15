package com.example.passpoint.feature.answer.data.dto.response

/**
 * POST /api/v1/answers 응답 (201)
 */
data class AnswerResponse(
    val answerId: Long,
    val questionId: Long,
    val type: String,
    val status: String,
    val createdAt: String
)
