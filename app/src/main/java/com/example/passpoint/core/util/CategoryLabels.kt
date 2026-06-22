package com.example.passpoint.core.util

// 학습 기록/북마크 화면에서 카테고리 코드를 한글 라벨로 보여줄 때 쓴다.
fun categoryKorLabel(value: String): String = when (value) {
    "CS" -> "CS"
    "LANGUAGE" -> "언어"
    "SPRING" -> "Spring"
    "DATA_STRUCTURE" -> "자료구조"
    "ALGORITHM" -> "알고리즘"
    "DATABASE" -> "데이터베이스"
    "SECURITY" -> "보안"
    "INFRA" -> "인프라"
    "SW_ARCHITECTURE" -> "아키텍처"
    "WEB" -> "웹"
    else -> value
}

// 질문 상세/피드백 화면에서 카테고리 코드를 영문 타이틀 케이스로 보여줄 때 쓴다 (예: SPRING_CORE -> Spring Core).
fun categoryLabel(value: String): String =
    value.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }
