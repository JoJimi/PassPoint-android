package com.example.passpoint.feature.question.ui

/**
 * 질문 목록 정렬 옵션.
 * value는 Spring Data Page의 sort 파라미터 형식("필드,방향")을 따른다.
 */
enum class SortOption(val label: String, val value: String) {
    LATEST("최신순", "createdAt,desc"),
    OLDEST("오래된순", "createdAt,asc");

    companion object {
        val default = LATEST
    }
}