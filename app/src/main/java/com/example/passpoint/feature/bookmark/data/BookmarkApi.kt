package com.example.passpoint.feature.bookmark.data

import com.example.passpoint.core.dto.response.PageResponse
import com.example.passpoint.feature.bookmark.data.dto.request.BookmarkCreateRequest
import com.example.passpoint.feature.bookmark.data.dto.response.BookmarkResponse
import com.example.passpoint.feature.bookmark.data.dto.response.BookmarkSummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookmarkApi {

    @GET("api/v1/bookmarks")
    suspend fun getList(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<BookmarkSummaryResponse>

    /**
     * 즐겨찾기 추가(멱등). 이미 등록돼 있으면 새로 안 만들고 기존 항목을 반환한다.
     */
    @POST("api/v1/bookmarks")
    suspend fun add(@Body request: BookmarkCreateRequest): BookmarkResponse

    @DELETE("api/v1/bookmarks/{questionId}")
    suspend fun remove(@Path("questionId") questionId: Long): Response<Unit>
}
