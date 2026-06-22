package com.example.passpoint.feature.auth.ui

/**
 * 로그인 화면의 상태
 * 화면은 이 상태만 보고 무엇을 그릴지 결정한다.
 */
sealed interface LoginUiState {
    data object Idle : LoginUiState        // 아무것도 안 한 초기 상태
    data object Loading : LoginUiState     // 로그인 진행 중 (스피너)
    data object Success : LoginUiState     // 성공 → 화면별 후속 동작(홈 이동, 로그인 화면 복귀 등)
    data class Error(val message: String) : LoginUiState  // 실패 → 메시지 표시
}