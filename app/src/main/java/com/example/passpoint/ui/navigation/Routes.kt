package com.example.passpoint.ui.navigation

/**
 * 화면 주소(route) 모음.
 * 문자열을 직접 쓰면 오타나기 쉬워서 한 곳에 모아둔다.
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val QUESTION_LIST = "questions"
    const val QUESTION_DETAIL = "questions/{id}"    // {id}는 이동할 때 채울 자리;

    // 상세로 이동할 때 실제 id를 끼워 주소를 만드는 헬퍼
    fun questionDetail(id: Long) = "questions/$id"
}