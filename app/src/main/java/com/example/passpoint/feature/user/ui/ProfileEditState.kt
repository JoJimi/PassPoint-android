package com.example.passpoint.feature.user.ui

/**
 * 마이페이지 프로필 수정 다이얼로그의 저장 상태.
 */
sealed interface ProfileEditState {
    data object Idle : ProfileEditState
    data object Saving : ProfileEditState
    data object Success : ProfileEditState
    data class Error(val message: String) : ProfileEditState
}
