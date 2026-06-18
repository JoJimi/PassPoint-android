package com.example.passpoint.feature.user.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.feature.user.data.dto.response.UserResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

private val PassPurple = Color(0xFF5B4FE8)
private val ScreenBg = Color(0xFFF7F7FA)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF2B2B33)
private val TextSecondary = Color(0xFF8E8E9A)
private val DangerColor = Color(0xFFE05252)

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onBookmarkClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profileEditState by viewModel.profileEditState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is MyPageUiState.LoggedOut) onLogout()
    }

    LaunchedEffect(profileEditState) {
        if (profileEditState is ProfileEditState.Success) {
            showEditDialog = false
            viewModel.consumeProfileEditState()
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "마이페이지",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        when (val state = uiState) {
            is MyPageUiState.Loading, is MyPageUiState.LoggedOut -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PassPurple)
                }
            }
            is MyPageUiState.Error -> {
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
            is MyPageUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProfileSection(
                        profile = state.profile,
                        onEditClick = { showEditDialog = true }
                    )
                    StatsSection(stats = state.stats)
                    MenuSection(
                        isLoggingOut = state.isLoggingOut,
                        onBookmarkClick = onBookmarkClick,
                        onLogoutClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("로그아웃 하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout(context)
                }) {
                    Text("로그아웃", color = DangerColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    val editingProfile = (uiState as? MyPageUiState.Success)?.profile
    if (showEditDialog && editingProfile != null) {
        ProfileEditDialog(
            profile = editingProfile,
            editState = profileEditState,
            onDismiss = {
                showEditDialog = false
                viewModel.consumeProfileEditState()
            },
            onSave = { nickname, statusMessage ->
                viewModel.updateProfile(nickname, statusMessage)
            }
        )
    }
}

@Composable
private fun ProfileEditDialog(
    profile: UserResponse,
    editState: ProfileEditState,
    onDismiss: () -> Unit,
    onSave: (nickname: String, statusMessage: String) -> Unit
) {
    var nickname by remember { mutableStateOf(profile.nickname) }
    var statusMessage by remember { mutableStateOf(profile.statusMessage ?: "") }
    val isSaving = editState is ProfileEditState.Saving

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("프로필 수정") },
        text = {
            Column {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("닉네임") },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = statusMessage,
                    onValueChange = { statusMessage = it },
                    label = { Text("상태 메시지") },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editState is ProfileEditState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = editState.message,
                        color = DangerColor,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(nickname.trim(), statusMessage) },
                enabled = !isSaving
            ) {
                Text(if (isSaving) "저장 중..." else "저장", color = PassPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun ProfileSection(
    profile: UserResponse,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile.nickname,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "수정",
                    fontSize = 13.sp,
                    color = PassPurple,
                    modifier = Modifier.clickable { onEditClick() }
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = profile.email,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = profile.statusMessage ?: "상태 메시지가 없어요.",
                fontSize = 13.sp,
                color = TextSecondary
            )
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
private fun MenuSection(
    isLoggingOut: Boolean,
    onBookmarkClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                MenuRow(label = "북마크", trailing = "›", enabled = true, onClick = onBookmarkClick)
                Divider(color = ScreenBg)
                MenuRow(
                    label = if (isLoggingOut) "로그아웃 중..." else "로그아웃",
                    trailing = null,
                    enabled = !isLoggingOut,
                    labelColor = DangerColor,
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
private fun MenuRow(
    label: String,
    trailing: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    labelColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = if (enabled) labelColor else TextSecondary
        )
        if (trailing != null) {
            Text(
                text = trailing,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
