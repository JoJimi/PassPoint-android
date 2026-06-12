package com.example.passpoint.feature.auth.data.dto.request

/**
 * 소셜 로그인 요청 바디
 * 앱이 구글에서 받은 ID Token을 백엔드로 보낸다.
 */
data class LoginRequest(
    val idToken: String
)
