package com.example.passpoint.core.dto.response

data class PagedResponse<T>(
    val content: List<T>,
    val page: PageInfo
) {
    val isLast: Boolean get() = page.number >= page.totalPages - 1
}
