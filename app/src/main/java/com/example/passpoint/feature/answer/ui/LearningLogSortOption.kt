package com.example.passpoint.feature.answer.ui

/**
 * 학습 기록(답변 목록) 정렬 옵션.
 * value는 Spring Data Page의 sort 파라미터 형식("필드,방향")을 따른다.
 */
enum class LearningLogSortOption(val label: String, val value: String) {
    LATEST("최신순", "createdAt,desc"),
    SCORE_DESC("점수 높은순", "score,desc"),
    SCORE_ASC("점수 낮은순", "score,asc");

    companion object {
        val default = LATEST
    }
}
