package com.example.passpoint.feature.question.data

import com.example.passpoint.core.dto.response.PageResponse
import com.example.passpoint.feature.question.data.dto.response.QuestionDetailResponse
import com.example.passpoint.feature.question.data.dto.response.QuestionSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestionApi {

    /**
     * 질문 검색
     * GET /api/v1/questions?keyword=...&category=...&page=0&size=20
     * 응답이 Spring Page<>라 PageResponse로 감싸서 받는다.
     */
    @GET("api/v1/questions")
    suspend fun search(
        @Query("keyword") keyword: String?,
        @Query("category") category: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<QuestionSearchResponse>

    /**
     * 질문 상세
     * GET /api/v1/questions/{id}
     */
    @GET("api/v1/questions/{id}")
    suspend fun getDetail(
        @Path("id") id: Long
    ): QuestionDetailResponse
}