package com.example.passpoint.feature.fcm.data

import com.example.passpoint.feature.fcm.data.dto.request.FcmTokenRequest
import com.example.passpoint.feature.fcm.data.dto.response.FcmTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface FcmApi {

    /**
     * FCM 기기 토큰 등록.
     * POST /api/v1/users/me/fcm-token  (Authorization 헤더는 RetrofitClient가 자동 첨부)
     * 멱등: 같은 토큰이면 시간만 갱신, 다른 계정이 쓰던 토큰이면 소유자 이전.
     */
    @POST("api/v1/users/me/fcm-token")
    suspend fun registerToken(@Body request: FcmTokenRequest): FcmTokenResponse

    /**
     * FCM 기기 토큰 삭제 (로그아웃 등에서 사용). 응답 바디 없음(204).
     * DELETE /api/v1/users/me/fcm-token/{tokenId}
     */
    @DELETE("api/v1/users/me/fcm-token/{tokenId}")
    suspend fun deleteToken(@Path("tokenId") tokenId: Long): Response<Unit>
}
