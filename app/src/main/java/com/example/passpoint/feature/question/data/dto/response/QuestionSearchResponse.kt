package com.example.passpoint.feature.question.data.dto.response

data class QuestionSearchResponse(
    val id: Long,
    val title: String,
    val mainCategory: String,
    val subCategory: String,
    val difficulty: String,
    val tags: List<String>,
    val createdAt: String,
    val bookmarked: Boolean
)