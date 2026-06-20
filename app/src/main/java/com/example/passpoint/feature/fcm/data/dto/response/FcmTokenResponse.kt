package com.example.passpoint.feature.fcm.data.dto.response

/**
 * FCM 토큰 등록 응답 바디.
 * 멱등 처리되며, 같은 토큰 재등록 시 갱신 시각만 바뀐다.
 */
data class FcmTokenResponse(
    val tokenId: Long,
    val token: String,
    val createdAt: String
)
