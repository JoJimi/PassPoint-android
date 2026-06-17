package com.example.passpoint.feature.answer.data.dto.request

data class AnswerCreateRequest(
    val questionId: Long,
    val type: String,
    val answerText: String? = null,
    val audioKey: String? = null
)
