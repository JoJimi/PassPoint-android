package com.example.passpoint.ui.login

/**
 * 로그인 화면의 상태
 * 화면은 이 상태만 보고 무엇을 그릴지 결정한다.
 */
sealed interface LoginUiState {
    data object Idle : LoginUiState        // 아무것도 안 한 초기 상태
    data object Loading : LoginUiState     // 로그인 진행 중 (스피너)
    data object Success : LoginUiState     // 성공 → 홈으로 이동
    data class Error(val message: String) : LoginUiState  // 실패 → 메시지 표시
}