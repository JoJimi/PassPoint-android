package com.example.passpoint.core.network

import com.example.passpoint.core.dto.response.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException

/**
 * 서버 에러 응답(ErrorResponse)에서 사용자에게 보여줄 message를 꺼낸다.
 * 파싱에 실패하거나 HTTP 에러가 아니면 default를 반환한다.
 */
fun Throwable.toUserMessage(default: String): String {
    if (this is HttpException) {
        val body = response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            try {
                val error = Gson().fromJson(body, ErrorResponse::class.java)
                if (!error.message.isNullOrBlank()) return error.message
            } catch (_: Exception) {
                // 파싱 실패 시 default로 폴백
            }
        }
    }
    return message ?: default
}
