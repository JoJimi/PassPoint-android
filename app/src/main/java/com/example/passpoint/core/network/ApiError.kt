package com.example.passpoint.core.network

import com.example.passpoint.core.dto.response.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException

private const val DUPLICATE_EMAIL_CODE = "USER002"

/**
 * HttpException의 에러 바디를 ErrorResponse로 파싱한다.
 * errorBody()는 한 번만 읽을 수 있는 스트림이라 호출부에서 한 번만 불러야 한다.
 */
private fun Throwable.parseErrorResponse(): ErrorResponse? {
    if (this !is HttpException) return null
    val body = response()?.errorBody()?.string()
    if (body.isNullOrBlank()) return null
    return try {
        Gson().fromJson(body, ErrorResponse::class.java)
    } catch (_: Exception) {
        null
    }
}

/**
 * 서버 에러 응답(ErrorResponse)에서 사용자에게 보여줄 message를 꺼낸다.
 * 파싱에 실패하거나 HTTP 에러가 아니면 default를 반환한다.
 */
fun Throwable.toUserMessage(default: String): String {
    val error = parseErrorResponse()
    return error?.message?.takeIf { it.isNotBlank() } ?: (message ?: default)
}

/**
 * 구글/카카오 로그인 전용 메시지 변환.
 * 최초 소셜 로그인 시 같은 이메일이 다른 방식(이메일 또는 다른 소셜)으로 이미 가입돼 있으면
 * 서버가 409 USER002를 던진다. 서버는 보안상 어떤 provider로 가입돼 있었는지 알려주지 않으므로
 * 구체적인 방법 대신 "가입했던 방법으로 로그인해달라"는 안내로 대체한다.
 */
fun Throwable.toSocialLoginErrorMessage(default: String): String {
    val error = parseErrorResponse()
    if (error?.code == DUPLICATE_EMAIL_CODE) {
        return "이미 다른 방식으로 가입된 이메일이에요. 가입했던 방법으로 로그인해주세요."
    }
    return error?.message?.takeIf { it.isNotBlank() } ?: (message ?: default)
}
