package com.example.passpoint.feature.question.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.feature.answer.ui.AnswerSubmitState
import com.example.passpoint.feature.answer.ui.RecordingState
import com.example.passpoint.feature.question.data.dto.response.QuestionDetailResponse

// 시안에서 뽑은 색
private val PassPurple = Color(0xFF5B4FE8)
private val PassPurpleLight = Color(0xFFEEECFB)
private val ScreenBg = Color(0xFFF7F7FA)
private val TagBg = Color(0xFFF1F1F5)
private val TagText = Color(0xFF8E8E9A)
private val BorderColor = Color(0xFFE3E3EA)

private const val ANSWER_MAX_LENGTH = 3000

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

private fun categoryLabel(value: String): String =
    value.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

private fun formatRecordingTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
fun QuestionDetailScreen(
    id: Long,
    viewModel: QuestionDetailViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSubmitSuccess: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRecording()
        } else {
            Toast.makeText(context, "녹음 권한이 필요해요. 설정에서 허용해주세요.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(id) {
        viewModel.load(id)
    }

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
            Spacer(Modifier.width(22.dp))
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
                    recordingState = recordingState,
                    onSubmitText = { text -> viewModel.submitAnswer(state.question.id, text) },
                    onSubmitVoice = { viewModel.submitVoiceAnswer(state.question.id) },
                    onStartRecording = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.startRecording()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopRecording = { viewModel.stopRecording() },
                    onStartPlayback = { viewModel.startPlayback() },
                    onStopPlayback = { viewModel.stopPlayback() },
                    onResetRecording = { viewModel.resetRecording() }
                )
            }
        }
    }
}

@Composable
private fun QuestionDetailContent(
    question: QuestionDetailResponse,
    submitState: AnswerSubmitState,
    recordingState: RecordingState,
    onSubmitText: (String) -> Unit,
    onSubmitVoice: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onStartPlayback: () -> Unit,
    onStopPlayback: () -> Unit,
    onResetRecording: () -> Unit
) {
    var showHint by remember { mutableStateOf(false) }
    var answerMode by remember { mutableStateOf(AnswerMode.VOICE) }
    var answerText by remember { mutableStateOf("") }

    // 텍스트 모드로 전환 시 진행 중인 녹음/재생 초기화
    LaunchedEffect(answerMode) {
        if (answerMode == AnswerMode.TEXT) {
            onResetRecording()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
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

        Text(
            text = question.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B33)
        )

        Spacer(Modifier.height(12.dp))

        if (!question.hint.isNullOrBlank()) {
            HintToggle(
                hint = question.hint,
                expanded = showHint,
                onToggle = { showHint = !showHint }
            )
            Spacer(Modifier.height(16.dp))
        }

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
            AnswerMode.VOICE -> VoiceAnswerRecorder(
                recordingState = recordingState,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onStartPlayback = onStartPlayback,
                onStopPlayback = onStopPlayback,
                onResetRecording = onResetRecording
            )
            AnswerMode.TEXT -> TextAnswerInput(
                text = answerText,
                onTextChange = { if (it.length <= ANSWER_MAX_LENGTH) answerText = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        val isSubmitting = submitState is AnswerSubmitState.Submitting
        val canSubmit = when (answerMode) {
            AnswerMode.VOICE ->
                (recordingState is RecordingState.Recorded || recordingState is RecordingState.Playing) && !isSubmitting
            AnswerMode.TEXT -> answerText.isNotBlank() && !isSubmitting
        }

        Button(
            onClick = {
                when (answerMode) {
                    AnswerMode.VOICE -> onSubmitVoice()
                    AnswerMode.TEXT -> onSubmitText(answerText)
                }
            },
            enabled = canSubmit,
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

@Composable
private fun VoiceAnswerRecorder(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onStartPlayback: () -> Unit,
    onStopPlayback: () -> Unit,
    onResetRecording: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (recordingState) {
            is RecordingState.Idle -> {
                Text(
                    text = "버튼을 눌러 음성으로 답변하세요",
                    fontSize = 14.sp,
                    color = Color(0xFF6B6B76)
                )
                Spacer(Modifier.height(20.dp))
                RecordToggleButton(isRecording = false, onClick = onStartRecording)
                Spacer(Modifier.height(12.dp))
                Text("탭하여 녹음 시작", fontSize = 12.sp, color = TagText)
            }

            is RecordingState.Recording -> {
                Text(
                    text = "녹음 중...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE5484D)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = formatRecordingTime(recordingState.elapsedSeconds),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2B33)
                )
                Spacer(Modifier.height(20.dp))
                RecordToggleButton(isRecording = true, onClick = onStopRecording)
                Spacer(Modifier.height(12.dp))
                Text("탭하여 정지", fontSize = 12.sp, color = TagText)
            }

            is RecordingState.Recorded -> {
                Text(
                    text = "녹음 완료",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E9E5B)
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onResetRecording,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B6B76))
                    ) {
                        Text("다시 녹음")
                    }
                    Button(
                        onClick = onStartPlayback,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
                    ) {
                        Text("▶ 재생")
                    }
                }
            }

            is RecordingState.Playing -> {
                Text(
                    text = "재생 중...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PassPurple
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onResetRecording,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B6B76))
                    ) {
                        Text("다시 녹음")
                    }
                    Button(
                        onClick = onStopPlayback,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
                    ) {
                        Text("⏹ 정지")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordToggleButton(isRecording: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (isRecording) Color(0xFFE5484D) else PassPurple)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isRecording) "⏹" else "🎤",
            fontSize = 28.sp
        )
    }
}

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
