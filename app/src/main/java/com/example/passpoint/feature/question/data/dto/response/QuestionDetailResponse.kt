package com.example.passpoint.feature.question.data.dto.response

data class QuestionDetailResponse(
    val id: Long,
    val mainCategory: String,
    val subCategory: String,
    val difficulty: String,
    val title: String,
    val content: String,
    val hint: String?
)