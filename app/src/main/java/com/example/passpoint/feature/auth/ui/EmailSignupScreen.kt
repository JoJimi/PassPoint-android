package com.example.passpoint.feature.auth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val PassPurple = Color(0xFF5B4FE8)
private val ScreenBg = Color(0xFFF7F7FA)
private val TextPrimary = Color(0xFF2B2B33)
private val TextSecondary = Color(0xFF8E8E9A)

@Composable
fun EmailSignupScreen(
    viewModel: EmailAuthViewModel = viewModel(),
    onSignupSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState is LoginUiState.Loading

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            Toast.makeText(context, "회원가입이 완료됐어요. 로그인해주세요.", Toast.LENGTH_SHORT).show()
            onSignupSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기", tint = TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Text("이메일로 회원가입", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("이메일, 비밀번호, 닉네임만 입력하면 바로 시작할 수 있어요.", fontSize = 13.sp, color = TextSecondary)

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호 (8~64자)") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Text(
                        text = if (passwordVisible) "🙈" else "👁",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(enabled = !isLoading) { passwordVisible = !passwordVisible }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("비밀번호 확인") },
                singleLine = true,
                enabled = !isLoading,
                isError = passwordMismatch,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Text(
                        text = if (confirmPasswordVisible) "🙈" else "👁",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(enabled = !isLoading) { confirmPasswordVisible = !confirmPasswordVisible }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (passwordMismatch) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "비밀번호가 일치하지 않아요.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("닉네임 (1~20자)") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState is LoginUiState.Error) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.signupWithEmail(email.trim(), password, confirmPassword, nickname.trim())
                },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() &&
                    confirmPassword.isNotBlank() && !passwordMismatch && nickname.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("회원가입", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("이미 계정이 있으신가요? ", fontSize = 13.sp, color = TextSecondary)
                Text(
                    text = "로그인",
                    fontSize = 13.sp,
                    color = PassPurple,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToLogin() }
                )
            }
        }
    }
}
