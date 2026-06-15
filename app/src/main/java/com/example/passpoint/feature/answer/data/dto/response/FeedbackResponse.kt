package com.example.passpoint.feature.answer.data.dto.response

data class FeedbackResponse(
    val answerId: Long,
    val score: Int,
    val accuracyScore: Int,
    val structureScore: Int,
    val completenessScore: Int,
    val goodPoints: List<String>,
    val improvementPoints: List<String>,
    val modelAnswer: String,
    val coveredKeywords: List<String>,
    val createdAt: String
)
