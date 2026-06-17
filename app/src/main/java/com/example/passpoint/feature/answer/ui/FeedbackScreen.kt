package com.example.passpoint.feature.answer.ui

import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.feature.answer.data.dto.response.AnswerDetailResponse
import com.example.passpoint.feature.answer.data.dto.response.FeedbackResponse
import com.example.passpoint.feature.answer.data.dto.response.QuestionInfo

// 시안에서 뽑은 색
private val PassPurple = Color(0xFF5B4FE8)
private val PassPurpleLight = Color(0xFFEEECFB)
private val ScreenBg = Color(0xFFF7F7FA)
private val TagBg = Color(0xFFF1F1F5)
private val TagText = Color(0xFF8E8E9A)
private val GoodColor = Color(0xFF2E9E5B)
private val GoodBg = Color(0xFFE6F4EA)
private val ImproveColor = Color(0xFFE8912D)
private val ImproveBg = Color(0xFFFFF3E0)

// ENUM 형태 카테고리 값을 보기 좋은 라벨로 변환 (예: SPRING_CORE -> Spring Core)
private fun categoryLabel(value: String): String =
    value.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

@Composable
fun FeedbackScreen(
    answerId: Long,
    viewModel: FeedbackViewModel = viewModel(),
    onBack: () -> Unit = {},
    onRetryQuestion: (Long) -> Unit = {},
    onGoToList: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(answerId) {
        viewModel.load(answerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.weight(1f))
            Text(text = "피드백 결과", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))

            var bookmarked by remember { mutableStateOf(false) }
            Text(
                text = if (bookmarked) "⭐" else "☆",
                fontSize = 20.sp,
                modifier = Modifier.clickable { bookmarked = !bookmarked }
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is FeedbackUiState.Loading -> {
                    CircularProgressIndicator(color = PassPurple)
                }
                is FeedbackUiState.Error -> {
                    RetryMessage(
                        message = state.message,
                        onRetry = { viewModel.reload(answerId) }
                    )
                }
                is FeedbackUiState.Success -> {
                    val feedback = state.answer.feedback
                    if (feedback == null) {
                        RetryMessage(
                            message = "피드백을 불러올 수 없어요.",
                            onRetry = { viewModel.reload(answerId) }
                        )
                    } else {
                        FeedbackContent(
                            answer = state.answer,
                            feedback = feedback,
                            onRetryQuestion = { onRetryQuestion(state.answer.question.id) },
                            onGoToList = onGoToList
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RetryMessage(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2B2B33))
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
        ) {
            Text("다시 시도", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FeedbackContent(
    answer: AnswerDetailResponse,
    feedback: FeedbackResponse,
    onRetryQuestion: () -> Unit,
    onGoToList: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            QuestionInfoCard(question = answer.question)

            if (answer.type == "VOICE" && answer.audioUrl != null) {
                Spacer(Modifier.height(16.dp))
                VoicePlaybackBar(
                    audioUrl = answer.audioUrl,
                    durationSeconds = answer.audioDuration
                )
                if (!answer.answerText.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    SttTextCard(text = answer.answerText)
                }
            }

            Spacer(Modifier.height(20.dp))

            ScoreSection(score = feedback.score)

            Spacer(Modifier.height(20.dp))

            SubScoresRow(feedback = feedback)

            Spacer(Modifier.height(20.dp))

            PointsSection(
                title = "잘된 포인트",
                icon = "✅",
                items = feedback.goodPoints,
                badgeColor = GoodColor,
                itemBg = GoodBg
            )

            Spacer(Modifier.height(16.dp))

            PointsSection(
                title = "개선하면 더 좋은 포인트",
                icon = "💡",
                items = feedback.improvementPoints,
                badgeColor = ImproveColor,
                itemBg = ImproveBg
            )

            Spacer(Modifier.height(16.dp))

            KeywordsSection(keywords = feedback.coveredKeywords)

            Spacer(Modifier.height(16.dp))

            ModelAnswerToggle(modelAnswer = feedback.modelAnswer)

            Spacer(Modifier.height(20.dp))
        }

        // 하단 액션 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetryQuestion,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PassPurple),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PassPurple)
            ) {
                Text("다시 풀기", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onGoToList,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
            ) {
                Text("답변 기록 확인", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 질문 정보 카드 (카테고리 + 제목)
@Composable
private fun QuestionInfoCard(question: QuestionInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PassPurpleLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = categoryLabel(question.mainCategory),
                    color = PassPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = question.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B33)
            )
        }
    }
}

// 종합 점수 (원형) + 한줄평
@Composable
private fun ScoreSection(score: Int) {
    val (emoji, label) = scoreLabel(score)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScoreCircle(score = score)

        Spacer(Modifier.height(12.dp))

        Text(
            text = "$emoji $label",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B33)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = scoreDescription(score),
            fontSize = 13.sp,
            color = TagText,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

private fun scoreLabel(score: Int): Pair<String, String> = when {
    score >= 90 -> "🎉" to "완벽해요!"
    score >= 75 -> "👏" to "훌륭해요!"
    score >= 60 -> "👍" to "좋아요!"
    else -> "💪" to "조금 더 보완해봐요"
}

private fun scoreDescription(score: Int): String = when {
    score >= 75 -> "핵심 내용을 구체적으로 설명했어요.\n다른 사람이 들어도 이해하기 쉬운 답변이에요."
    score >= 60 -> "기본 개념은 잘 잡았어요.\n조금만 더 보완하면 훨씬 좋아질 거예요."
    else -> "핵심 개념을 다시 한 번 정리해보면 좋겠어요."
}

// 종합 점수 원형 그래프
@Composable
private fun ScoreCircle(score: Int) {
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            drawArc(
                color = PassPurpleLight,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = PassPurple,
                startAngle = -90f,
                sweepAngle = 360f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2B2B33)
            )
            Text("/ 100", fontSize = 13.sp, color = TagText)
        }
    }
}

// 정확성 / 구성력 / 충분성 서브 점수
@Composable
private fun SubScoresRow(feedback: FeedbackResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SubScoreCard(label = "정확성", score = feedback.accuracyScore, modifier = Modifier.weight(1f))
        SubScoreCard(label = "구성력", score = feedback.structureScore, modifier = Modifier.weight(1f))
        SubScoreCard(label = "충분성", score = feedback.completenessScore, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SubScoreCard(label: String, score: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PassPurple)
        Spacer(Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = TagText)
    }
}

// 잘된 포인트 / 개선하면 더 좋은 포인트
@Composable
private fun PointsSection(
    title: String,
    icon: String,
    items: List<String>,
    badgeColor: Color,
    itemBg: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${items.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(itemBg)
                        .padding(12.dp)
                ) {
                    Text(text = icon, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = point,
                        fontSize = 13.sp,
                        color = Color(0xFF3C3C46),
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

// 다룬 키워드 (칩)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordsSection(keywords: List<String>) {
    Column {
        Text(text = "다룬 키워드", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keywords.forEach { keyword ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TagBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = keyword, fontSize = 12.sp, color = Color(0xFF5B5B66))
                }
            }
        }
    }
}

// 음성 재생 바 (VOICE 답변 전용)
@Composable
private fun VoicePlaybackBar(audioUrl: String, durationSeconds: Int?) {
    var isPlaying by remember { mutableStateOf(false) }
    var isPreparing by remember { mutableStateOf(false) }
    val player = remember { MediaPlayer() }

    DisposableEffect(audioUrl) {
        onDispose {
            try { if (player.isPlaying) player.stop() } catch (_: Exception) {}
            player.release()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isPreparing) Color(0xFFBBBBBB) else PassPurple)
                .clickable(enabled = !isPreparing) {
                    if (isPlaying) {
                        try { player.pause() } catch (_: Exception) {}
                        isPlaying = false
                    } else {
                        try {
                            player.reset()
                            player.setDataSource(audioUrl)
                            player.setOnCompletionListener { isPlaying = false }
                            player.setOnPreparedListener { mp ->
                                isPreparing = false
                                mp.start()
                                isPlaying = true
                            }
                            player.prepareAsync()
                            isPreparing = true
                        } catch (_: Exception) {
                            isPreparing = false
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isPreparing) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(text = if (isPlaying) "⏸" else "▶", fontSize = 18.sp, color = Color.White)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "음성 답변",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2B2B33)
            )
            if (durationSeconds != null) {
                Text(
                    text = formatDuration(durationSeconds),
                    fontSize = 12.sp,
                    color = TagText
                )
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

// STT 변환 결과 카드
@Composable
private fun SttTextCard(text: String) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PassPurpleLight)
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "음성 인식 텍스트 보기",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PassPurple
            )
            Spacer(Modifier.weight(1f))
            Text(if (expanded) "▾" else "▸", fontSize = 12.sp, color = PassPurple)
        }

        if (expanded) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Text(text, fontSize = 13.sp, color = Color(0xFF5B5B66), lineHeight = 20.sp)
            }
        }
    }
}

// 모범답안 보기/숨기기 토글
@Composable
private fun ModelAnswerToggle(modelAnswer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "모범답안 보기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B33)
            )
            Spacer(Modifier.weight(1f))
            Text(if (expanded) "▾" else "▸", fontSize = 14.sp, color = TagText)
        }

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PassPurpleLight)
                    .padding(14.dp)
            ) {
                Text(
                    text = modelAnswer,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF3C3C46)
                )
            }
        }
    }
}
