package com.example.passpoint.feature.user.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.feature.user.data.dto.response.UserResponse
import com.example.passpoint.feature.user.data.dto.response.UserStatsResponse

private val PassPurple = Color(0xFF5B4FE8)
private val PassPurpleLight = Color(0xFFEEECFB)
private val ScreenBg = Color(0xFFF7F7FA)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF2B2B33)
private val TextSecondary = Color(0xFF8E8E9A)
private val DangerColor = Color(0xFFE05252)
private const val NICKNAME_MAX_LENGTH = 20
private const val STATUS_MESSAGE_MAX_LENGTH = 50

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

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PassPurpleLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🙂", fontSize = 26.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text("프로필 수정", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                Spacer(Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("닉네임", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { if (it.length <= NICKNAME_MAX_LENGTH) nickname = it },
                        singleLine = true,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${nickname.length}/$NICKNAME_MAX_LENGTH",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("상태 메시지", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = statusMessage,
                        onValueChange = { if (it.length <= STATUS_MESSAGE_MAX_LENGTH) statusMessage = it },
                        placeholder = { Text("상태 메시지를 입력해보세요", color = TextSecondary, fontSize = 13.sp) },
                        singleLine = true,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${statusMessage.length}/$STATUS_MESSAGE_MAX_LENGTH",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.End
                    )
                }

                if (editState is ProfileEditState.Error) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEB), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(editState.message, color = DangerColor, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onSave(nickname.trim(), statusMessage) },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PassPurple)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("저장", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(4.dp))

                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("취소", color = TextSecondary)
                }
            }
        }
    }
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
            MenuRow(
                icon = "🔖",
                title = "북마크",
                subtitle = "저장한 질문을 모아볼 수 있어요.",
                onClick = onBookmarkClick
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (isLoggingOut) "로그아웃 중..." else "로그아웃",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isLoggingOut) TextSecondary else DangerColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoggingOut) { onLogoutClick() }
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun MenuRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PassPurpleLight, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
        Text("›", fontSize = 18.sp, color = TextSecondary)
    }
}
