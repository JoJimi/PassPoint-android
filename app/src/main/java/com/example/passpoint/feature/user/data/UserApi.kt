package com.example.passpoint.feature.user.data

import com.example.passpoint.feature.user.data.dto.response.UserResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse
import retrofit2.http.GET

interface UserApi {

    @GET("api/v1/users/me/stats")
    suspend fun getStats(): UserStatsResponse

    @GET("api/v1/users/me")
    suspend fun getMe(): UserResponse
}
