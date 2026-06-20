package com.example.passpoint.feature.fcm.data

import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.fcm.data.dto.request.FcmTokenRequest
import com.example.passpoint.feature.fcm.data.dto.response.FcmTokenResponse

class FcmRepository {
    private val fcmApi = RetrofitClient.fcmApi

    suspend fun registerToken(token: String): FcmTokenResponse =
        fcmApi.registerToken(FcmTokenRequest(token = token))

    suspend fun deleteToken(tokenId: Long) {
        fcmApi.deleteToken(tokenId)
    }
}
