package com.example.passpoint.feature.auth.data

import android.content.Context
import com.example.passpoint.core.local.TokenManager
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.auth.data.dto.request.RefreshRequest
import com.example.passpoint.feature.auth.data.dto.response.TokenResponse
import com.example.passpoint.feature.fcm.FcmTokenRegistrar

/**
 * 로그인/가입 성공 직후 공통으로 해야 하는 일.
 * 구글/카카오/이메일 로그인·이메일 회원가입 모두 토큰을 받은 뒤 이 두 가지를 동일하게 수행한다.
 */
suspend fun completeLogin(context: Context, tokens: TokenResponse) {
    TokenManager(context).saveTokens(tokens.accessToken, tokens.refreshToken)
    // 로그인 전이라 보류됐던 FCM 토큰을 이제 서버에 등록한다.
    FcmTokenRegistrar.fetchAndRegister(context.applicationContext)
}

class AuthRepository(context: Context) {

    private val authApi = RetrofitClient.authApi
    private val tokenManager = TokenManager(context.applicationContext)

    /**
     * 로그아웃. 서버에 refreshToken 블랙리스트 등록을 요청한 뒤 로컬 토큰을 지운다.
     * 서버 요청이 실패해도(네트워크 오류 등) 로컬 토큰은 항상 지워서 로그인 화면으로 보낼 수 있게 한다.
     */
    suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken != null) {
            try {
                authApi.logout(RefreshRequest(refreshToken))
            } catch (_: Exception) {
            }
        }
        tokenManager.clearTokens()
    }
}

