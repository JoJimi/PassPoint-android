package com.example.passpoint.feature.answer.data

import com.example.passpoint.feature.answer.data.dto.request.AnswerCreateRequest
import com.example.passpoint.feature.answer.data.dto.response.AnswerDetailResponse
import com.example.passpoint.feature.answer.data.dto.response.AnswerResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AnswerApi {

    /**
     * 답변 제출
     * POST /api/v1/answers
     * 2주차는 동기 처리라 응답 시점에 이미 status가 DONE/FAILED로 확정된다.
     */
    @POST("api/v1/answers")
    suspend fun submit(@Body request: AnswerCreateRequest): AnswerResponse

    /**
     * 답변 단건 조회 (상태 + 피드백)
     * GET /api/v1/answers/{id}
     */
    @GET("api/v1/answers/{id}")
    suspend fun getDetail(@Path("id") id: Long): AnswerDetailResponse
}
