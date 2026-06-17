package com.example.passpoint.core.dto.response

data class PageInfo(
    val size: Int,
    val number: Int,
    val totalElements: Long,
    val totalPages: Int
)
