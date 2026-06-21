package com.example.passpoint.ui.navigation

/**
 * 화면 주소(route) 모음.
 * 문자열을 직접 쓰면 오타나기 쉬워서 한 곳에 모아둔다.
 */
object Routes {
    const val LOGIN = "login"
    const val EMAIL_LOGIN = "email_login"
    const val EMAIL_SIGNUP = "email_signup"
    const val HOME = "home"
    const val QUESTION_LIST = "questions"
    // 홈의 카테고리 칩에서 들어가는 전용 화면. 하단 탭의 QUESTION_LIST와 라우트를 분리해서
    // 뒤로가기/탭 전환 로직이 서로 얽히지 않게 한다(뒤로가기 버튼으로 들어왔다 나가는 화면).
    const val CATEGORY_QUESTIONS = "category_questions/{category}"
    const val QUESTION_DETAIL = "questions/{id}"    // {id}는 이동할 때 채울 자리;
    const val ANSWER_PROCESSING = "answers/{id}/processing"
    const val ANSWER_FEEDBACK = "answers/{id}/feedback"
    const val LEARNING_LOG = "learning_log"
    const val MY_PAGE = "my_page"
    const val BOOKMARK_LIST = "bookmarks"

    // 카테고리로 필터링된 질문 목록으로 이동할 때 (예: 홈의 카테고리 칩)
    fun categoryQuestions(category: String) = "category_questions/$category"

    // 상세로 이동할 때 실제 id를 끼워 주소를 만드는 헬퍼
    fun questionDetail(id: Long) = "questions/$id"

    // 답변 제출 후 처리 중 화면으로 이동할 때 answerId를 끼워 주소를 만드는 헬퍼
    fun answerProcessing(answerId: Long) = "answers/$answerId/processing"

    // 처리 완료 후 피드백 결과 화면으로 이동할 때 answerId를 끼워 주소를 만드는 헬퍼
    fun answerFeedback(answerId: Long) = "answers/$answerId/feedback"
}