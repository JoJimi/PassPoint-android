package com.example.passpoint.feature.auth.data.dto.request

/**
 * 카카오 로그인 요청 바디
 * 앱이 카카오 SDK에서 받은 액세스 토큰을 백엔드로 보낸다 (구글과 달리 ID 토큰이 아님).
 */
data class KakaoLoginRequest(
    val accessToken: String
)
