package com.example.passpoint.feature.auth.data

import com.example.passpoint.feature.auth.data.dto.request.LoginRequest
import com.example.passpoint.feature.auth.data.dto.request.RefreshRequest
import com.example.passpoint.feature.auth.data.dto.response.TokenResponse
import retrofit2.Response
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
    ): TokenResponse

    /**
     * 토큰 재발급
     * POST /api/v1/auth/refresh
     * refreshToken을 보내면 새 accessToken/refreshToken을 발급한다.
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest
    ): TokenResponse

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     * refreshToken을 블랙리스트에 등록해 더 이상 재발급에 쓰지 못하게 한다.
     * 응답 바디가 없을 수 있어 Response<Unit>으로 받는다.
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: RefreshRequest
    ): Response<Unit>
}