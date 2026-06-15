package com.example.passpoint.feature.answer.data

import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.answer.data.dto.request.AnswerCreateRequest
import com.example.passpoint.feature.answer.data.dto.response.AnswerDetailResponse
import com.example.passpoint.feature.answer.data.dto.response.AnswerResponse

/**
 * 답변 제출/조회 데이터 접근.
 * ViewModel은 이 Repository만 알면 되고, 실제 통신(AnswerApi)은 여기서 숨긴다.
 */
class AnswerRepository {

    private val answerApi = RetrofitClient.answerApi

    /**
     * 텍스트 답변 제출.
     */
    suspend fun submitAnswer(questionId: Long, answerText: String): AnswerResponse {
        return answerApi.submit(
            AnswerCreateRequest(questionId = questionId, type = "TEXT", answerText = answerText)
        )
    }

    /**
     * 답변 상세 조회 (상태 + 피드백).
     */
    suspend fun getAnswerDetail(id: Long): AnswerDetailResponse {
        return answerApi.getDetail(id)
    }
}
