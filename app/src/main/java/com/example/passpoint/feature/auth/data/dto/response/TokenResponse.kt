package com.example.passpoint.feature.auth.data.dto.response

/**
 * 토큰 재발급 응답 바디
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
