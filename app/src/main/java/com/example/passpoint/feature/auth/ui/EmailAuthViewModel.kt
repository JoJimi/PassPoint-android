package com.example.passpoint.feature.auth.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.core.network.toUserMessage
import com.example.passpoint.feature.auth.data.completeLogin
import com.example.passpoint.feature.auth.data.dto.request.EmailLoginRequest
import com.example.passpoint.feature.auth.data.dto.request.EmailSignupRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

/**
 * 이메일 로그인/회원가입 화면이 공유하는 ViewModel.
 */
class EmailAuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginWithEmail(context: Context, email: String, password: String) {
        if (!EMAIL_REGEX.matches(email)) {
            _uiState.value = LoginUiState.Error("올바른 이메일 형식이 아니에요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val tokens = RetrofitClient.authApi.loginEmail(
                    EmailLoginRequest(email = email, password = password)
                )
                completeLogin(context, tokens)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                Log.e("EmailAuthViewModel", "Email login failed", e)
                _uiState.value = LoginUiState.Error(e.toUserMessage("로그인에 실패했어요."))
            }
        }
    }

    fun signupWithEmail(email: String, password: String, nickname: String) {
        val validationError = validate(email, password, nickname)
        if (validationError != null) {
            _uiState.value = LoginUiState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                // 가입 응답에 토큰이 같이 오지만 저장하지 않는다 — 가입 직후 자동 로그인 대신
                // 사용자가 입력한 자격증명으로 로그인 화면에서 직접 재로그인하게 한다.
                RetrofitClient.authApi.signupEmail(
                    EmailSignupRequest(email = email, password = password, nickname = nickname)
                )
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                Log.e("EmailAuthViewModel", "Email signup failed", e)
                _uiState.value = LoginUiState.Error(e.toUserMessage("회원가입에 실패했어요."))
            }
        }
    }

    // 서버 왕복 없이 바로 알 수 있는 형식 오류는 미리 걸러서 보여준다 (명세서 3번 항목의 길이 제약과 동일).
    private fun validate(email: String, password: String, nickname: String): String? = when {
        !EMAIL_REGEX.matches(email) -> "올바른 이메일 형식이 아니에요."
        password.length !in 8..64 -> "비밀번호는 8~64자로 입력해주세요."
        nickname.isBlank() || nickname.length > 20 -> "닉네임은 1~20자로 입력해주세요."
        else -> null
    }
}
