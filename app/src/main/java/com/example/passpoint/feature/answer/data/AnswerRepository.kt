package com.example.passpoint.feature.answer.data

import com.example.passpoint.core.dto.response.PageResponse
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.answer.data.dto.request.AnswerCreateRequest
import com.example.passpoint.feature.answer.data.dto.request.AudioPresignedUrlRequest
import com.example.passpoint.feature.answer.data.dto.response.AnswerDetailResponse
import com.example.passpoint.feature.answer.data.dto.response.AnswerResponse
import com.example.passpoint.feature.answer.data.dto.response.AnswerSummaryResponse
import com.example.passpoint.feature.answer.data.dto.response.AudioPresignedUrlResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AnswerRepository {

    private val answerApi = RetrofitClient.answerApi
    private val storageApi = RetrofitClient.storageApi

    suspend fun submitAnswer(questionId: Long, answerText: String): AnswerResponse {
        return answerApi.submit(
            AnswerCreateRequest(questionId = questionId, type = "TEXT", answerText = answerText)
        )
    }

    suspend fun getAnswerDetail(id: Long): AnswerDetailResponse {
        return answerApi.getDetail(id)
    }

    /**
     * 음성 업로드용 presigned URL 발급.
     * 반환된 uploadUrl로 직접 PUT, key는 POST /answers(VOICE) 시 audioKey로 전달.
     */
    suspend fun getAudioPresignedUrl(): AudioPresignedUrlResponse {
        return answerApi.getAudioPresignedUrl(AudioPresignedUrlRequest())
    }

    /**
     * presigned URL로 m4a 파일을 직접 PUT 업로드.
     * Authorization 헤더 없이 전송 — storageApi는 인증 인터셉터를 타지 않는다.
     */
    suspend fun uploadAudio(uploadUrl: String, file: File) {
        val requestBody = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val response = storageApi.uploadFile(uploadUrl, requestBody)
        if (!response.isSuccessful) {
            error("MinIO 업로드 실패: HTTP ${response.code()}")
        }
    }

    // F8에서 구현: 음성 답변 제출 (POST /answers, type=VOICE, audioKey)
    suspend fun submitVoiceAnswer(questionId: Long, audioKey: String): AnswerResponse {
        return answerApi.submit(
            AnswerCreateRequest(questionId = questionId, type = "VOICE", audioKey = audioKey)
        )
    }

    suspend fun getAnswerList(
        category: String? = null,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<AnswerSummaryResponse> {
        return answerApi.getList(category = category, page = page, size = size)
    }
}
