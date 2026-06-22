package com.example.passpoint.feature.auth.ui

import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.ui.theme.BorderColor
import com.example.passpoint.ui.theme.PassPointTheme
import com.example.passpoint.ui.theme.PassPurple
import com.example.passpoint.ui.theme.ScreenBg
import com.example.passpoint.ui.theme.TextPrimary
import com.example.passpoint.ui.theme.TextSecondary

// 로고 그라데이션 전용 색상 (공용 PassPurpleLight와 값이 달라 별도로 둔다)
private val LogoGradientLight = Color(0xFF8B7FF5)
private val KakaoYellow = Color(0xFFFEE500)

/**
 * 로그인 화면
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToEmailLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState is LoginUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageDropdown()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PassPoint",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PassPurple
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "기술 면접, 포인트로 합격에 가까워지다",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(28.dp))
            AppLogo()
            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                onClick = { viewModel.loginWithGoogle(context) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = PassPurple
                    )
                } else {
                    GoogleMark()
                    Spacer(Modifier.width(8.dp))
                    Text("Google로 계속하기", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { viewModel.loginWithKakao(context) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KakaoYellow)
            ) {
                Text("💬", fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Text("카카오로 계속하기", color = Color(0xFF1A1A1A), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = BorderColor)
                Text("  또는  ", fontSize = 12.sp, color = TextSecondary)
                Divider(modifier = Modifier.weight(1f), color = BorderColor)
            }

            Spacer(Modifier.height(18.dp))

            OutlinedButton(
                onClick = onNavigateToEmailLogin,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text("✉️", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text("이메일로 로그인", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            if (uiState is LoginUiState.Error) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(28.dp))
            FeatureHighlights()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LanguageDropdown() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("한국어") }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = "🌐 $selected", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.width(4.dp))
            Text(text = "▾", fontSize = 11.sp, color = TextSecondary)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("한국어") },
                onClick = {
                    selected = "한국어"
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    expanded = false
                    Toast.makeText(context, "English는 준비 중이에요.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun AppLogo() {
    Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(listOf(LogoGradientLight, PassPurple)),
                    shape = RoundedCornerShape(24.dp)
                )
        )
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = (-16).dp, y = 16.dp)
                    .rotate(45f)
                    .background(Color.White, RoundedCornerShape(3.dp))
            )
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("P.", color = PassPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GoogleMark() {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, BorderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("G", color = Color(0xFF4285F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FeatureHighlights() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FeatureCard(modifier = Modifier.weight(1f), icon = "🎯", title = "포인트로 학습하기", caption = "포인트 적립")
        FeatureCard(modifier = Modifier.weight(1f), icon = "🧩", title = "내 역량을 알아보기", caption = "맞춤 질문")
        FeatureCard(modifier = Modifier.weight(1f), icon = "📈", title = "실력 향상 한눈에", caption = "분석 데이터")
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    caption: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(caption, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    PassPointTheme {
        LoginScreen()
    }
}
