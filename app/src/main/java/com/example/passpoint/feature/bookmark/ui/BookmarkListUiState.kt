package com.example.passpoint.feature.bookmark.ui

import com.example.passpoint.feature.bookmark.data.dto.response.BookmarkSummaryResponse

sealed interface BookmarkListUiState {
    data object Loading : BookmarkListUiState
    data class Error(val message: String) : BookmarkListUiState
    data class Success(
        val bookmarks: List<BookmarkSummaryResponse>,
        val isLoadingMore: Boolean = false,
        val isLastPage: Boolean = false
    ) : BookmarkListUiState
}
