package com.example.passpoint.feature.bookmark.data

import com.example.passpoint.core.dto.response.PageResponse
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.bookmark.data.dto.request.BookmarkCreateRequest
import com.example.passpoint.feature.bookmark.data.dto.response.BookmarkSummaryResponse

class BookmarkRepository {

    private val bookmarkApi = RetrofitClient.bookmarkApi

    suspend fun getList(page: Int = 0, size: Int = 20): PageResponse<BookmarkSummaryResponse> =
        bookmarkApi.getList(page = page, size = size)

    suspend fun add(questionId: Long) {
        bookmarkApi.add(BookmarkCreateRequest(questionId))
    }

    suspend fun remove(questionId: Long) {
        bookmarkApi.remove(questionId)
    }
}
