package com.example.passpoint.feature.user.data

import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.user.data.dto.request.UserProfileUpdateRequest
import com.example.passpoint.feature.user.data.dto.response.UserResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

class UserRepository {
    private val userApi = RetrofitClient.userApi

    suspend fun getStats(): UserStatsResponse = userApi.getStats()

    suspend fun getMe(): UserResponse = userApi.getMe()

    suspend fun updateProfile(nickname: String?, statusMessage: String?): UserResponse =
        userApi.updateProfile(UserProfileUpdateRequest(nickname = nickname, statusMessage = statusMessage))
}
