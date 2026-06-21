package com.example.passpoint.feature.auth.data.dto.request

/**
 * 이메일 회원가입 요청 바디
 */
data class EmailSignupRequest(
    val email: String,
    val password: String,
    val nickname: String
)
