package com.example.passpoint.feature.auth.data.dto.request

/**
 * 이메일 로그인 요청 바디
 */
data class EmailLoginRequest(
    val email: String,
    val password: String
)
