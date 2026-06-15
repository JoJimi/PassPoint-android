package com.example.passpoint.core.dto.response

/**
 * 공통 에러 응답 포맷.
 * code로 분기하고, message는 사용자에게 그대로 노출해도 되는 한국어 문구.
 */
data class ErrorResponse(
    val code: String?,
    val message: String?,
    val status: Int?,
    val timestamp: String?,
    val path: String?
)
