package com.example.passpoint.feature.user.ui

import com.example.passpoint.feature.user.data.dto.response.UserResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

sealed interface MyPageUiState {
    data object Loading : MyPageUiState
    data class Error(val message: String) : MyPageUiState
    data class Success(
        val profile: UserResponse,
        val stats: UserStatsResponse,
        val isLoggingOut: Boolean = false
    ) : MyPageUiState
    data object LoggedOut : MyPageUiState
}
