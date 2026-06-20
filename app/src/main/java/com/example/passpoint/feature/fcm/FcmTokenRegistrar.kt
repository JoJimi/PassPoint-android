package com.example.passpoint.feature.fcm

import android.content.Context
import android.util.Log
import com.example.passpoint.core.local.TokenManager
import com.example.passpoint.feature.fcm.data.FcmRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * FCM 토큰을 가져와 Logcat에 찍고, 로그인 상태면 백엔드에 등록한다.
 *
 * - 앱 시작 시(MainActivity): [fetchAndRegister]로 현재 토큰을 조회해 등록 시도
 * - 토큰 갱신 시(MyFirebaseMessagingService.onNewToken): [register]로 새 토큰 등록
 * - 로그인 직후(LoginViewModel): [fetchAndRegister]로 다시 등록 (로그인 전엔 보류됐던 것 반영)
 */
object FcmTokenRegistrar {
    private const val TAG = "FCM"

    private val repository = FcmRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 현재 기기 토큰을 조회해 로그로 찍고, 로그인 상태면 서버에 등록한다. */
    fun fetchAndRegister(context: Context) {
        val appContext = context.applicationContext
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "토큰 가져오기 실패", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d(TAG, "FCM 토큰: $token")
                register(appContext, token)
            }
    }

    /** 토큰을 서버에 등록한다. 로그인 전이면(accessToken 없음) 보류한다. */
    fun register(context: Context, token: String) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                val accessToken = TokenManager(appContext).getAccessToken()
                if (accessToken == null) {
                    Log.d(TAG, "로그인 전이라 토큰 등록 보류 (로그인 후 다시 등록됨)")
                    return@launch
                }
                repository.registerToken(token)
                Log.d(TAG, "FCM 토큰 서버 등록 완료")
            } catch (e: Exception) {
                Log.w(TAG, "FCM 토큰 서버 등록 실패", e)
            }
        }
    }
}
