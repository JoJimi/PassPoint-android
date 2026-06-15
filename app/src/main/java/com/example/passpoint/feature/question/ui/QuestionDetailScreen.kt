package com.example.passpoint.feature.question.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.feature.answer.ui.AnswerSubmitState
import com.example.passpoint.feature.question.data.dto.response.QuestionDetailResponse

// 시안에서 뽑은 색
private val PassPurple = Color(0xFF5B4FE8)
private val PassPurpleLight = Color(0xFFEEECFB)
private val ScreenBg = Color(0xFFF7F7FA)
private val TagBg = Color(0xFFF1F1F5)
private val TagText = Color(0xFF8E8E9A)
private val BorderColor = Color(0xFFE3E3EA)

private const val ANSWER_MAX_LENGTH = 3000

// 답변 방식
private enum class AnswerMode(val label: String, val icon: String) {
    VOICE("음성으로 답변", "🎤"),
    TEXT("텍스트로 답변", "⌨")
}

private fun difficultyLabel(value: String): String = when (value) {
    "EASY" -> "쉬움"
    "MEDIUM" -> "보통"
    "HARD" -> "어려움"
    else -> value
}

// ENUM 형태 카테고리 값을 보기 좋은 라벨로 변환 (예: SPRING_CORE -> Spring Core)
private fun categoryLabel(value: String): String =
    value.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

@Composable
fun QuestionDetailScreen(
    id: Long,
    viewModel: QuestionDetailViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSubmitSuccess: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(id) {
        viewModel.load(id)
    }

    // 제출 결과(성공/실패)는 1회만 처리하고 Idle로 되돌린다.
    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is AnswerSubmitState.Success -> {
                onSubmitSuccess(state.answerId)
                viewModel.consumeSubmitState()
            }
            is AnswerSubmitState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.consumeSubmitState()
            }
            else -> Unit
        }
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
            Text(text = "질문 상세", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(22.dp)) // 좌우 균형용
        }

        when (val state = uiState) {
            is QuestionDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PassPurple)
                }
            }
            is QuestionDetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is QuestionDetailUiState.Success -> {
                QuestionDetailContent(
                    question = state.question,
                    submitState = submitState,
                    onSubmit = { answerText -> viewModel.submitAnswer(state.question.id, answerText) }
                )
            }
        }
    }
}

@Composable
private fun QuestionDetailContent(
    question: QuestionDetailResponse,
    submitState: AnswerSubmitState,
    onSubmit: (String) -> Unit
) {
    var showHint by remember { mutableStateOf(false) }
    var answerMode by remember { mutableStateOf(AnswerMode.VOICE) }
    var answerText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // 카테고리 뱃지 (왼쪽) / 난이도 뱃지 (오른쪽)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryBadge(text = categoryLabel(question.mainCategory))
                CategoryBadge(text = categoryLabel(question.subCategory))
            }
            DifficultyBadge(value = question.difficulty)
        }

        Spacer(Modifier.height(12.dp))

        // 제목
        Text(
            text = question.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B33)
        )

        Spacer(Modifier.height(12.dp))

        // 힌트 토글
        if (!question.hint.isNullOrBlank()) {
            HintToggle(
                hint = question.hint,
                expanded = showHint,
                onToggle = { showHint = !showHint }
            )
            Spacer(Modifier.height(16.dp))
        }

        // 본문
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                text = question.content,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color(0xFF3C3C46),
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // 나의 답변 방식
        Text(text = "나의 답변 방식", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnswerMode.values().forEach { mode ->
                AnswerModeCard(
                    mode = mode,
                    selected = mode == answerMode,
                    modifier = Modifier.weight(1f),
                    onClick = { answerMode = mode }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (answerMode) {
            AnswerMode.VOICE -> VoiceAnswerPlaceholder()
            AnswerMode.TEXT -> TextAnswerInput(
                text = answerText,
                onTextChange = { if (it.length <= ANSWER_MAX_LENGTH) answerText = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        val isSubmitting = submitState is AnswerSubmitState.Submitting

        Button(
            onClick = { onSubmit(answerText) },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("제출하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// 카테고리 뱃지
@Composable
private fun CategoryBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PassPurpleLight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = PassPurple, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// 난이도 뱃지 (난이도별로 색을 구분해서 카테고리 뱃지와 헷갈리지 않도록 표시)
@Composable
private fun DifficultyBadge(value: String) {
    val (bg, text) = when (value) {
        "EASY" -> Color(0xFFE6F4EA) to Color(0xFF2E9E5B)
        "MEDIUM" -> Color(0xFFFFF3E0) to Color(0xFFE8912D)
        "HARD" -> Color(0xFFFDEAEA) to Color(0xFFE5484D)
        else -> TagBg to TagText
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "난이도 ${difficultyLabel(value)}",
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// 힌트 보기/숨기기 토글
@Composable
private fun HintToggle(hint: String, expanded: Boolean, onToggle: () -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (expanded) "힌트 숨기기" else "힌트 보기",
                fontSize = 13.sp,
                color = PassPurple,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            Text(if (expanded) "▾" else "▸", fontSize = 12.sp, color = PassPurple)
        }

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PassPurpleLight)
                    .padding(12.dp)
            ) {
                Text(hint, fontSize = 13.sp, color = Color(0xFF5B5B66), lineHeight = 20.sp)
            }
        }
    }
}

// 답변 방식 선택 카드 (음성 / 텍스트)
@Composable
private fun AnswerModeCard(
    mode: AnswerMode,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PassPurpleLight else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) PassPurple else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(mode.icon, fontSize = 20.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = mode.label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) PassPurple else Color(0xFF6B6B76)
        )
    }
}

// 음성 답변 영역 (자리만 - 실제 녹음 기능은 추후 연동)
@Composable
private fun VoiceAnswerPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("버튼을 눌러 음성으로 답변하세요", fontSize = 14.sp, color = Color(0xFF6B6B76))
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PassPurple)
                .clickable { /* TODO: 음성 녹음 기능 연동 */ },
            contentAlignment = Alignment.Center
        ) {
            Text("🎤", fontSize = 28.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("예상 시간 90초", fontSize = 12.sp, color = TagText)
    }
}

// 텍스트 답변 입력 영역
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextAnswerInput(text: String, onTextChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = { Text("텍스트로 답변해주세요...") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PassPurple,
                unfocusedBorderColor = BorderColor
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${text.length} / $ANSWER_MAX_LENGTH",
            fontSize = 12.sp,
            color = TagText,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
