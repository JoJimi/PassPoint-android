package com.example.passpoint.feature.fcm.data.dto.request

/**
 * FCM 기기 토큰 등록 요청 바디.
 * 백엔드(FcmTokenController)는 단일 필드 "token"(@NotBlank)을 기대한다.
 * 필드명을 바꾸면 400(검증 실패)이 떨어지니 반드시 "token"으로 유지할 것.
 */
data class FcmTokenRequest(
    val token: String
)
