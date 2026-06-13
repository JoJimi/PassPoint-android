package com.example.passpoint.core.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 앱 전역에 DataStore 인스턴스 하나 (Context 확장으로 선언)
private val Context.dataStore by preferencesDataStore(name = "auth")

/**
 * JWT 토큰을 DataStore에 저장/조회/삭제한다.
 */
class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    // 저장 (로그인 성공 시)
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    // accessToken 흐름으로 구독 (있으면 값, 없으면 null)
    val accessTokenFlow: Flow<String?> =
        context.dataStore.data.map { preferences -> preferences[ACCESS_TOKEN] }

    // 한 번만 꺼내 쓰기 (Interceptor에서 동기적으로 필요할 때)
    suspend fun getAccessToken(): String? =
        context.dataStore.data.first()[ACCESS_TOKEN]

    // 한 번만 꺼내 쓰기 (Authenticator에서 토큰 재발급할 때)
    suspend fun getRefreshToken(): String? =
        context.dataStore.data.first()[REFRESH_TOKEN]

    // 삭제 (로그아웃 시)
    suspend fun clearTokens() {
        context.dataStore.edit { it.clear() }
    }
}