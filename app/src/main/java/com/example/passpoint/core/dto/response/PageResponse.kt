package com.example.passpoint.core.dto.response

/**
 * Spring의 Page<T> 응답을 받는 공용 껍데기.
 * 실제 목록은 content 안에 들어있다.
 */
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,            // 현재 페이지 번호 (0부터)
    val size: Int,
    val last: Boolean           // 마지막 페이지 여부
)