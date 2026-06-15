package com.example.passpoint.feature.answer.domain

/**
 * 서버의 AnswerStatus와 1:1 대응.
 * PENDING(접수) -> TRANSCRIBING(음성 변환중, 3주차) -> ANALYZING(AI 분석중) -> DONE(완료) / FAILED(실패)
 */
enum class AnswerStatus {
    PENDING,
    TRANSCRIBING,
    ANALYZING,
    DONE,
    FAILED,
    UNKNOWN;

    companion object {
        fun from(value: String): AnswerStatus =
            entries.find { it.name == value } ?: UNKNOWN
    }
}
