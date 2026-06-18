package com.example.passpoint.feature.user.data

import com.example.passpoint.feature.user.data.dto.request.UserProfileUpdateRequest
import com.example.passpoint.feature.user.data.dto.response.UserResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApi {

    @GET("api/v1/users/me/stats")
    suspend fun getStats(): UserStatsResponse

    @GET("api/v1/users/me")
    suspend fun getMe(): UserResponse

    /**
     * 프로필 부분 수정. null인 필드는 기존 값 유지, statusMessage는 ""를 보내면 비울 수 있다.
     */
    @PATCH("api/v1/users/me")
    suspend fun updateProfile(@Body request: UserProfileUpdateRequest): UserResponse
}
