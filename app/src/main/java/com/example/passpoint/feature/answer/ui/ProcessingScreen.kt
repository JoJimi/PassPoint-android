package com.example.passpoint.feature.answer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// 시안에서 뽑은 색
private val PassPurple = Color(0xFF5B4FE8)
private val PassPurpleLight = Color(0xFFEEECFB)
private val ScreenBg = Color(0xFFF7F7FA)
private val TagText = Color(0xFF8E8E9A)

/**
 * 처리 중(분석 중) 화면.
 * 2주차는 동기 처리라 폴링 첫 응답에서 바로 DONE이 와서 이 화면은 잠깐 스쳐 지나간다.
 * 3주차 비동기 전환 후에는 폴링이 실제로 여러 번 도는 동안 이 화면이 유지된다.
 */
@Composable
fun ProcessingScreen(
    answerId: Long,
    viewModel: ProcessingViewModel = viewModel(),
    onDone: (Long) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(answerId) {
        viewModel.startPolling(answerId)
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is ProcessingUiState.Done) {
            onDone(state.answerId)
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
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "피드백 분석 중", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ProcessingUiState.Loading, is ProcessingUiState.Done -> AnalyzingContent()
                is ProcessingUiState.Failed -> RetryContent(
                    message = "답변 분석에 실패했어요.",
                    onRetry = onRetry
                )
                is ProcessingUiState.Timeout -> RetryContent(
                    message = "분석이 지연되고 있어요. 잠시 후 다시 시도해주세요.",
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun AnalyzingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PassPurple,
                strokeWidth = 4.dp,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PassPurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 28.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "답변을 분석하고 있어요",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B33)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "정확성, 구성력, 충분성을 종합적으로 평가하고 있어요",
            fontSize = 13.sp,
            color = TagText
        )
    }
}

@Composable
private fun RetryContent(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = message,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2B2B33)
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
        ) {
            Text("다시 시도", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
