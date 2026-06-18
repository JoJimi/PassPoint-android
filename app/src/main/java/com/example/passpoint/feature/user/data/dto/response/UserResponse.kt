package com.example.passpoint.feature.user.data.dto.response

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val statusMessage: String?
)
