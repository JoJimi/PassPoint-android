package com.example.passpoint.feature.user.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.feature.auth.data.AuthRepository
import com.example.passpoint.feature.user.data.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyPageViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow<MyPageUiState>(MyPageUiState.Loading)
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

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
}
