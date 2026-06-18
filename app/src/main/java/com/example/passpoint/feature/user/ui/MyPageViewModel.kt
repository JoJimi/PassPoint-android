package com.example.passpoint.feature.user.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.core.network.toUserMessage
import com.example.passpoint.feature.auth.data.AuthRepository
import com.example.passpoint.feature.user.data.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val NICKNAME_MAX_LENGTH = 20
private const val STATUS_MESSAGE_MAX_LENGTH = 50

class MyPageViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow<MyPageUiState>(MyPageUiState.Loading)
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private val _profileEditState = MutableStateFlow<ProfileEditState>(ProfileEditState.Idle)
    val profileEditState: StateFlow<ProfileEditState> = _profileEditState.asStateFlow()

    init {
        loadMyPage()
    }

    private fun loadMyPage() {
        _uiState.value = MyPageUiState.Loading
        viewModelScope.launch {
            try {
                coroutineScope {
                    val profileDeferred = async { userRepository.getMe() }
                    val statsDeferred = async { userRepository.getStats() }
                    _uiState.value = MyPageUiState.Success(
                        profile = profileDeferred.await(),
                        stats = statsDeferred.await()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MyPageUiState.Error(e.message ?: "불러오기 실패")
            }
        }
    }

    fun refresh() = loadMyPage()

    fun logout(context: Context) {
        val currentState = _uiState.value as? MyPageUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoggingOut = true)
            AuthRepository(context).logout()
            _uiState.value = MyPageUiState.LoggedOut
        }
    }

    /**
     * 닉네임/상태메시지 부분 수정. statusMessage에 빈 문자열을 넘기면 비울 수 있다.
     */
    fun updateProfile(nickname: String, statusMessage: String) {
        if (nickname.isBlank() || nickname.length > NICKNAME_MAX_LENGTH) {
            _profileEditState.value = ProfileEditState.Error("닉네임은 1~${NICKNAME_MAX_LENGTH}자로 입력해주세요.")
            return
        }
        if (statusMessage.length > STATUS_MESSAGE_MAX_LENGTH) {
            _profileEditState.value = ProfileEditState.Error("상태 메시지는 ${STATUS_MESSAGE_MAX_LENGTH}자 이하로 입력해주세요.")
            return
        }

        val currentState = _uiState.value as? MyPageUiState.Success ?: return
        viewModelScope.launch {
            _profileEditState.value = ProfileEditState.Saving
            try {
                val updated = userRepository.updateProfile(nickname, statusMessage)
                _uiState.value = currentState.copy(profile = updated)
                _profileEditState.value = ProfileEditState.Success
            } catch (e: Exception) {
                _profileEditState.value = ProfileEditState.Error(e.toUserMessage("프로필 수정에 실패했어요."))
            }
        }
    }

    fun consumeProfileEditState() {
        _profileEditState.value = ProfileEditState.Idle
    }
}
