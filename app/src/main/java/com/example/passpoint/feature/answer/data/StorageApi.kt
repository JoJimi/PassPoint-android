package com.example.passpoint.feature.answer.data

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url

/**
 * MinIO/S3 presigned URL로 직접 PUT 업로드하는 인터페이스.
 * - 인증 인터셉터가 없는 별도 Retrofit 인스턴스에 연결된다.
 * - presigned URL 자체에 서명이 있으므로 Authorization 헤더를 붙이면 서명 불일치로 실패한다.
 * - Content-Type은 RequestBody에 설정된 값으로 자동 전송된다.
 */
interface StorageApi {

    @PUT
    suspend fun uploadFile(
        @Url uploadUrl: String,
        @Body body: RequestBody
    ): Response<ResponseBody>
}
