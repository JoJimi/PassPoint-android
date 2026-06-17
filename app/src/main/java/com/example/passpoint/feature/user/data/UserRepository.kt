package com.example.passpoint.feature.user.data

import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

class UserRepository {
    private val userApi = RetrofitClient.userApi

    suspend fun getStats(): UserStatsResponse = userApi.getStats()
}
