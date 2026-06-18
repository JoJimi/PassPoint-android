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
    const val ANSWER_PROCESSING = "answers/{id}/processing"
    const val ANSWER_FEEDBACK = "answers/{id}/feedback"
    const val LEARNING_LOG = "learning_log"
    const val MY_PAGE = "my_page"
    const val BOOKMARK_LIST = "bookmarks"

    // 상세로 이동할 때 실제 id를 끼워 주소를 만드는 헬퍼
    fun questionDetail(id: Long) = "questions/$id"

    // 답변 제출 후 처리 중 화면으로 이동할 때 answerId를 끼워 주소를 만드는 헬퍼
    fun answerProcessing(answerId: Long) = "answers/$answerId/processing"

    // 처리 완료 후 피드백 결과 화면으로 이동할 때 answerId를 끼워 주소를 만드는 헬퍼
    fun answerFeedback(answerId: Long) = "answers/$answerId/feedback"
}