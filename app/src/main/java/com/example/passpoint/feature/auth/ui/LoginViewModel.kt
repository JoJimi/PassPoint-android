package com.example.passpoint.feature.auth.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.BuildConfig
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.core.network.toSocialLoginErrorMessage
import com.example.passpoint.feature.auth.data.completeLogin
import com.example.passpoint.feature.auth.data.dto.request.KakaoLoginRequest
import com.example.passpoint.feature.auth.data.dto.request.LoginRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoginViewModel : ViewModel() {

    // 화면이 구독할 상태. 처음엔 Idle.
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * 구글 로그인 시작.
     * @param context Credential Manager를 띄우려면 화면(Activity) context가 필요하다.
     */
    fun loginWithGoogle(context: Context) {
        // 네트워크가 끼는 작업이라 코루틴에서 실행 (화면 안 멈춤)
        viewModelScope.launch {
            _uiState.value= LoginUiState.Loading
            try {
                // 1) 구글에서 ID Token 받기
                val idToken = getGoogleIdToken(context)

                // 2) 백엔드로 보내서 우리 서비스 토큰 받기
                val tokens = RetrofitClient.authApi.loginWithGoogle(
                    LoginRequest(idToken = idToken)
                )

                completeLogin(context, tokens)
                Log.d("LoginViewModel", "로그인 성공! accessToken = ${tokens.accessToken}")
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Google login failed", e)
                _uiState.value = LoginUiState.Error(e.toSocialLoginErrorMessage("로그인에 실패했어요."))
            }
        }
    }

    /**
     * 카카오 로그인 시작.
     * 카카오톡 앱이 있으면 그 앱으로, 없으면 카카오 계정(웹)으로 로그인한다.
     */
    fun loginWithKakao(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val accessToken = getKakaoAccessToken(context)
                val tokens = RetrofitClient.authApi.loginWithKakao(
                    KakaoLoginRequest(accessToken = accessToken)
                )
                completeLogin(context, tokens)
                _uiState.value = LoginUiState.Success
            } catch (e: KakaoLoginCancelledException) {
                // 사용자가 의도적으로 취소한 경우라 에러로 보여주지 않는다.
                _uiState.value = LoginUiState.Idle
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Kakao login failed", e)
                _uiState.value = LoginUiState.Error(e.toSocialLoginErrorMessage("카카오 로그인에 실패했어요."))
            }
        }
    }

    private class KakaoLoginCancelledException : Exception()

    /**
     * 카카오 SDK의 콜백 기반 로그인 API를 코루틴으로 감싼다.
     * 카카오톡 로그인이 안 되면(미설치 등) 카카오 계정 로그인으로 폴백한다.
     */
    private suspend fun getKakaoAccessToken(context: Context): String =
        suspendCancellableCoroutine { cont ->
            val accountCallback = { token: OAuthToken?, error: Throwable? ->
                when {
                    token != null -> cont.resume(token.accessToken)
                    error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                        cont.resumeWithException(KakaoLoginCancelledException())
                    else -> cont.resumeWithException(error ?: IllegalStateException("카카오 로그인에 실패했어요."))
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    when {
                        token != null -> cont.resume(token.accessToken)
                        error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                            cont.resumeWithException(KakaoLoginCancelledException())
                        else -> UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
            }
        }

    /**
     * Credential Manager를 띄워 구글 ID Token 문자열을 받아온다.
     */
    private suspend fun getGoogleIdToken (context: Context): String {
        val activity = context.findActivity()
        val credentialManager = CredentialManager.create(activity)

        // "구글로 로그인" 옵션. serverClientId엔 웹 클라이언트 ID를 넣는다.
        val googleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        // 이 줄에서 계정 선택 바텀 시트가 뜨고, 사용자가 고를 때까지 기다린다.
        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = result.credential

        if(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // 진짜 구글 ID Token이 맞는지 확인하고, 받은 결과에서 ID Token만 뽑아낸다.
            return GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        }
        throw IllegalArgumentException ("지원하지 않는 Credential 타입입니다.")
    }
    private fun Context.findActivity(): Activity {
        return when(this) {
            is Activity -> this                             // 이미 Activity면 그대로
            is ContextWrapper -> baseContext.findActivity() // 감싸져 있으면 한 겹 벗겨서 재귀
            else -> throw IllegalArgumentException("Activity context를 찾을 수 없습니다.")
        }
    }
}