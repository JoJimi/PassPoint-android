package com.example.passpoint.feature.answer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.feature.answer.data.dto.response.AnswerSummaryResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

private val PassPurple = Color(0xFF5B4FE8)
private val PassPurpleLight = Color(0xFFEEECFB)
private val ScreenBg = Color(0xFFF7F7FA)
private val CardBg = Color.White
private val TagText = Color(0xFF8E8E9A)
private val TextPrimary = Color(0xFF2B2B33)
private val TextSecondary = Color(0xFF8E8E9A)
private val DoneColor = Color(0xFF5B4FE8)
private val PendingColor = Color(0xFFE8912D)
private val FailedColor = Color(0xFFE05252)

private fun categoryKorLabel(value: String): String = when (value) {
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

private fun formatDate(createdAt: String): String =
    createdAt.take(10).replace("-", ".")

@Composable
fun LearningLogScreen(
    viewModel: LearningLogViewModel = viewModel(),
    onAnswerClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "학습 기록",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        when (val state = uiState) {
            is LearningLogUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PassPurple)
                }
            }
            is LearningLogUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            }
            is LearningLogUiState.Success -> {
                val listState = rememberLazyListState()

                LaunchedEffect(listState) {
                    snapshotFlow {
                        val layoutInfo = listState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisible >= totalItems - 3
                    }.collect { isNearEnd ->
                        if (isNearEnd) viewModel.loadNextPage()
                    }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 통계 카드
                    item {
                        state.stats?.let { stats ->
                            StatsSection(stats = stats)
                        }
                    }

                    // 목록 헤더
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "답변 기록",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "최신순",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (state.answers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("아직 풀어본 질문이 없어요.", color = TextSecondary, fontSize = 15.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("질문을 풀고 내 기록을 쌓아보세요!", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(state.answers) { answer ->
                            AnswerHistoryCard(
                                answer = answer,
                                onClick = { onAnswerClick(answer.answerId) }
                            )
                        }
                    }

                    if (state.isLoadingMore) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = PassPurple,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSection(stats: UserStatsResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "나의 학습 현황",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${stats.totalAnswered}",
                label = "총 답변수",
                valueColor = PassPurple
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${stats.currentStreak}일",
                label = "연속 학습",
                valueColor = Color(0xFFE8912D)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${stats.averageScore}점",
                label = "평균 점수",
                valueColor = Color(0xFF2E9E5B)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${stats.bestScore}점",
                label = "최고 점수",
                valueColor = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AnswerHistoryCard(
    answer: AnswerSummaryResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 카테고리 뱃지
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PassPurpleLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = categoryKorLabel(answer.mainCategory),
                        color = PassPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 질문 제목
                Text(
                    text = answer.questionTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                // 날짜
                Text(
                    text = formatDate(answer.createdAt),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.width(12.dp))

            // 점수 or 상태 뱃지
            ScoreBadge(status = answer.status, score = answer.score)
        }
    }
}

@Composable
private fun ScoreBadge(status: String, score: Int?) {
    when (status) {
        "DONE" -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PassPurpleLight)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${score ?: 0}점",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DoneColor
                )
            }
        }
        "FAILED" -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFEBEB))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "실패",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FailedColor
                )
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFF3E0))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "처리 중",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PendingColor
                )
            }
        }
    }
}
