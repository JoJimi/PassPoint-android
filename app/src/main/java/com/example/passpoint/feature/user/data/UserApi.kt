package com.example.passpoint.feature.user.data

import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse
import retrofit2.http.GET

interface UserApi {

    @GET("api/v1/users/me/stats")
    suspend fun getStats(): UserStatsResponse
}
