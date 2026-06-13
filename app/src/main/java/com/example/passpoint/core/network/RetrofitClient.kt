package com.example.passpoint.core.network

import android.content.Context
import com.example.passpoint.BuildConfig
import com.example.passpoint.core.local.TokenManager
import com.example.passpoint.feature.auth.data.AuthApi
import com.example.passpoint.feature.auth.data.dto.request.RefreshRequest
import com.example.passpoint.feature.question.data.QuestionApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

/**
 * Retrofit 인스턴스를 만들고, API 인터페이스 구현체를 제공한다.
 * 지금은 간단히 object(싱글톤)로 둔다. (나중에 Hilt 도입하면 옮길 수 있음)
 */
object RetrofitClient {

    private lateinit var tokenManager: TokenManager

    // 앱 시작 시 한 번 호출해서 Context를 넘겨준다 (MainActivity.onCreate에서 호출)
    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    // refreshToken마저 만료/무효해서 토큰을 비웠을 때 알리는 이벤트.
    // AppNavHost에서 이걸 구독해 로그인 화면으로 이동시킨다.
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    // 통신 내용을 Logcat에 찍어주는 인터셉터 (디버깅용)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 모든 요청이 서버로 나가기 직전, 저장된 토큰을 꺼내 헤더에 자동으로 붙인다.
    private val authInterceptor = Interceptor { chain ->
        // 저장해둔 accessToken 꺼내기 (suspend 함수라 runBlocking으로 잠깐 기다림)
        val token = runBlocking { tokenManager.getAccessToken() }

        val request = if (token != null) {
            // 토큰이 있으면 Authorization 헤더를 추가한 새 요청을 만든다
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            // 토큰이 없으면(로그인 전 등) 원래 요청 그대로 보낸다
            chain.request()
        }
        // 손본 요청을 다음 단계로 넘긴다
        chain.proceed(request)
    }

    /**
     * accessToken이 만료되어 401이 오면, refreshToken으로 재발급받아 같은 요청을 재시도한다.
     * refreshToken마저 없거나 재발급에 실패하면 토큰을 비우고 더 이상 재시도하지 않는다 (재로그인 필요).
     */
    private val tokenAuthenticator = Authenticator { _, response ->
        if (responseCount(response) >= 2) return@Authenticator null

        val refreshToken = runBlocking { tokenManager.getRefreshToken() }
            ?: return@Authenticator null

        runBlocking {
            try {
                val newTokens = refreshApi.refresh(RefreshRequest(refreshToken))
                tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } catch (_: Exception) {
                tokenManager.clearTokens()
                _sessionExpired.tryEmit(Unit)
                null
            }
        }
    }

    // 같은 요청에서 재발급 재시도가 또 401을 맞으면(무한루프 방지) 더 이상 시도하지 않는다.
    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)       // ① 토큰 헤더 먼저 붙이고
        .addInterceptor(loggingInterceptor)    // ② 그다음 통신 내용 로깅
        .authenticator(tokenAuthenticator)     // ③ 401이면 토큰 재발급 후 재시도
        .connectTimeout(30, TimeUnit.SECONDS)  // 연결 대기 30초
        .readTimeout(30, TimeUnit.SECONDS)     // 응답 대기 30초
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 토큰 재발급 전용 클라이언트. authInterceptor/authenticator를 타지 않아
    // 재발급 요청 자체가 401을 맞아 무한루프에 빠지는 것을 막는다.
    private val refreshRetrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(OkHttpClient.Builder().addInterceptor(loggingInterceptor).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val refreshApi: AuthApi = refreshRetrofit.create(AuthApi::class.java)

    // API 설계도 → 실제 동작하는 구현체
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val questionApi: QuestionApi = retrofit.create(QuestionApi::class.java)
}