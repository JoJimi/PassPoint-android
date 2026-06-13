package com.example.passpoint.feature.auth.data.dto.request

/**
 * 토큰 재발급 요청 바디
 */
data class RefreshRequest(
    val refreshToken: String
)
