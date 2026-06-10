package com.example.passpoint.data.dto.response

/**
 * 소셜 로그인 응답 바디
 * 백엔드가 ID Token을 검증한 뒤 발급해주는 우리 서비스용 토큰.
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)