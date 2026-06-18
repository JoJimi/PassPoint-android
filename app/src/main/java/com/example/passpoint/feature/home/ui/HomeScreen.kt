package com.example.passpoint.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
private val ScreenBg = Color(0xFFF7F7FA)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF2B2B33)
private val TextSecondary = Color(0xFF8E8E9A)

// 홈에서 바로 보여줄 카테고리 바로가기 (아이콘, 표시 라벨, API에 보낼 값)
private val homeCategories = listOf(
    Triple("💻", "CS", "CS"),
    Triple("☕", "언어 (Java)", "LANGUAGE"),
    Triple("🌱", "Spring", "SPRING"),
    Triple("🗂", "자료구조", "DATA_STRUCTURE")
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onGoToQuestions: () -> Unit = {},
    onSeeAllAnswers: () -> Unit = {},
    onAnswerClick: (Long) -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PassPoint",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PassPurple
            )
            Text(text = "🔔", fontSize = 18.sp)
        }

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PassPurple)
                }
            }
            is HomeUiState.Error -> {
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
            is HomeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    GreetingSection(nickname = state.nickname)
                    StatsSection(stats = state.stats)
                    CategoriesSection(
                        onCategoryClick = onCategoryClick,
                        onSeeAllClick = onGoToQuestions
                    )
                    RecentAnswersSection(
                        answers = state.recentAnswers,
                        onSeeAllClick = onSeeAllAnswers,
                        onAnswerClick = onAnswerClick,
                        onGoToQuestions = onGoToQuestions
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun GreetingSection(nickname: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = "안녕하세요, ${nickname}님!",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "오늘도 한 걸음 더 나아가봐요!",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatsSection(stats: UserStatsResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "🔥",
            value = "${stats.currentStreak}일",
            label = "연속 학습",
            valueColor = Color(0xFFE8912D)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "📝",
            value = "${stats.totalAnswered}개",
            label = "푼 질문",
            valueColor = PassPurple
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "📊",
            value = "${stats.averageScore}점",
            label = "평균 점수",
            valueColor = Color(0xFF2E9E5B)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "🏆",
            value = "${stats.bestScore}점",
            label = "최고 점수",
            valueColor = Color(0xFF2196F3)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
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
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(Modifier.height(2.dp))
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun CategoriesSection(
    onCategoryClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "카테고리",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "전체보기 ›",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            homeCategories.forEach { (icon, label, value) ->
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    icon = icon,
                    label = label,
                    onClick = { onCategoryClick(value) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentAnswersSection(
    answers: List<AnswerSummaryResponse>,
    onSeeAllClick: () -> Unit,
    onAnswerClick: (Long) -> Unit,
    onGoToQuestions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 푼 질문 다시 보기",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "전체보기 ›",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(Modifier.height(10.dp))

        if (answers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("아직 풀어본 질문이 없어요.", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onGoToQuestions,
                        colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
                    ) {
                        Text("질문 풀러 가기")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    answers.forEachIndexed { index, answer ->
                        RecentAnswerRow(answer = answer, onClick = { onAnswerClick(answer.answerId) })
                        if (index != answers.lastIndex) Divider(color = ScreenBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentAnswerRow(
    answer: AnswerSummaryResponse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = answer.questionTitle,
            fontSize = 14.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (answer.status == "DONE") "${answer.score ?: 0}점" else "처리 중",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = PassPurple
        )
        Spacer(Modifier.width(6.dp))
        Text(text = "›", fontSize = 16.sp, color = TextSecondary)
    }
}
