package com.example.passpoint.feature.bookmark.data.dto.response

data class BookmarkSummaryResponse(
    val questionId: Long,
    val title: String,
    val mainCategory: String,
    val bookmarkedAt: String
)
