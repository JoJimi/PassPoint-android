package com.example.passpoint.feature.answer.data.dto.response

data class AnswerSummaryResponse(
    val answerId: Long,
    val questionTitle: String,
    val mainCategory: String,
    val status: String,
    val score: Int?,
    val createdAt: String
)
