package com.example.passpoint.data.api

import com.example.passpoint.data.dto.request.LoginRequest
import com.example.passpoint.data.dto.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    /**
     * 소셜 로그인
     * POST /api/v1/auth/login/google
     * 구글 ID Token을 보내면 백엔드가 검증 후 우리 서비스용 토큰을 발급한다.
     */
    @POST("api/v1/auth/login/google")
    suspend fun loginWithGoogle(
        @Body request: LoginRequest
    ): LoginResponse
}