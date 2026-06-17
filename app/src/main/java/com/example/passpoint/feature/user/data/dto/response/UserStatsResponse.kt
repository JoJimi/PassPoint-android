package com.example.passpoint.feature.user.data.dto.response

data class UserStatsResponse(
    val currentStreak: Int,
    val totalAnswered: Int,
    val averageScore: Int,
    val bestScore: Int
)
