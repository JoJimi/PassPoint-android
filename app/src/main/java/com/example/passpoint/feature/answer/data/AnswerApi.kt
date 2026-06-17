package com.example.passpoint.feature.answer.data

import com.example.passpoint.feature.answer.data.dto.request.AnswerCreateRequest
import com.example.passpoint.feature.answer.data.dto.request.AudioPresignedUrlRequest
import com.example.passpoint.feature.answer.data.dto.response.AnswerDetailResponse
import com.example.passpoint.feature.answer.data.dto.response.AnswerResponse
import com.example.passpoint.feature.answer.data.dto.response.AudioPresignedUrlResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AnswerApi {

    @POST("api/v1/answers")
    suspend fun submit(@Body request: AnswerCreateRequest): AnswerResponse

    @GET("api/v1/answers/{id}")
    suspend fun getDetail(@Path("id") id: Long): AnswerDetailResponse

    /**
     * 음성 업로드용 presigned URL 발급
     * POST /api/v1/answers/audio/presignedurl
     * 응답의 uploadUrl로 직접 PUT 업로드 후, key를 POST /answers(VOICE)에 전달한다.
     */
    @POST("api/v1/answers/audio/presignedurl")
    suspend fun getAudioPresignedUrl(@Body request: AudioPresignedUrlRequest): AudioPresignedUrlResponse
}
