package com.example.passpoint.feature.question.data

import com.example.passpoint.core.dto.response.PageResponse
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.question.data.dto.response.QuestionDetailResponse
import com.example.passpoint.feature.question.data.dto.response.QuestionSearchResponse

/**
 * 질문 데이터 접근.
 * ViewModel은 이 Repository만 알면 되고, 실제 통신(QuestionApi)은 여기서 숨긴다.
 */
class QuestionRepository {

    private val questionApi = RetrofitClient.questionApi

    /**
     * 질문 검색.
     * 페이징 정보(last, totalPages 등)가 필요해서 PageResponse를 통째로 반환한다.
     * (목록은 ViewModel에서 .content로 꺼내 쓴다)
     */
    suspend fun search(
        keyword: String?,
        category: String?,
        page: Int,
        size: Int = 20
    ): PageResponse<QuestionSearchResponse> {
        return questionApi.search(keyword, category, page, size)
    }

    /**
     * 질문 상세 조회.
     */
    suspend fun getDetail(id: Long): QuestionDetailResponse {
        return questionApi.getDetail(id)
    }
}