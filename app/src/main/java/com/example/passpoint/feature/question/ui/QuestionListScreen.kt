package com.example.passpoint.feature.question.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passpoint.feature.question.data.dto.response.QuestionSearchResponse

// 시안에서 뽑은 색
private val PassPurple = Color(0xFF5B4FE8)
private val PassPurpleLight = Color(0xFFEEECFB)
private val ScreenBg = Color(0xFFF7F7FA)
private val TagBg = Color(0xFFF1F1F5)
private val TagText = Color(0xFF8E8E9A)

// 카테고리 필터 칩 목록 (전체 + 주요 카테고리)
private val categories = listOf(
    "전체" to null,
    "CS" to "CS",
    "언어 (Java)" to "LANGUAGE",
    "Spring" to "SPRING",
    "자료구조" to "DATA_STRUCTURE",
    "알고리즘" to "ALGORITHM",
    "데이터베이스" to "DATABASE",
    "보안" to "SECURITY",
    "인프라" to "INFRA",
    "아키텍처" to "SW_ARCHITECTURE",
    "웹" to "WEB"
)

@Composable
fun QuestionListScreen(
    viewModel: QuestionListViewModel = viewModel(),
    initialCategoryValue: String? = null,
    onBack: (() -> Unit)? = null,
    onQuestionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it.second == initialCategoryValue }?.first ?: "전체")
    }
    var selectedCategoryValue by remember { mutableStateOf(initialCategoryValue) }
    var selectedSort by remember { mutableStateOf(SortOption.default) }

    // 홈의 카테고리 칩에서 들어온 경우, 진입과 동시에 그 카테고리로 다시 검색한다.
    LaunchedEffect(initialCategoryValue) {
        if (initialCategoryValue != null) {
            viewModel.search(category = initialCategoryValue, sort = selectedSort.value)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // 헤더
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBack() }
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = "질문 목록",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        // 카테고리 필터 칩 (가로 스크롤)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { (label, value) ->
                CategoryChip(
                    label = label,
                    selected = label == selectedCategory,
                    onClick = {
                        selectedCategory = label
                        selectedCategoryValue = value
                        viewModel.search(category = value, sort = selectedSort.value)
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 본문 (상태별)
        when (val state = uiState) {
            is QuestionListUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PassPurple)
                }
            }
            is QuestionListUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is QuestionListUiState.Success -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("전체 ", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "${state.totalCount}개",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PassPurple
                        )
                    }

                    SortDropdown(
                        selected = selectedSort,
                        onSelect = { option ->
                            selectedSort = option
                            viewModel.search(category = selectedCategoryValue, sort = option.value)
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))

                if (state.questions.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("검색 결과가 없어요.", color = TagText)
                    }
                } else {
                    val listState = rememberLazyListState()

                    // 스크롤이 끝에서 3개 이내로 가까워지면 다음 페이지 요청
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            lastVisible >= totalItems - 3   // 끝 3개 이내?
                        }.collect { isNearEnd ->
                            if (isNearEnd) {
                                viewModel.loadNextPage()
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.questions) { question ->
                            QuestionCard(
                                question = question,
                                isBookmarked = question.id in bookmarkedIds,
                                onClick = { onQuestionClick(question.id) },
                                onToggleBookmark = { viewModel.toggleBookmark(question.id) }
                            )
                        }

                        // 하단 로딩 인디케이터 (다음 페이지 불러오는 중일 때만)
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
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
}

// 정렬 선택 드롭다운 (최신순 / 오래된순)
@Composable
private fun SortDropdown(
    selected: SortOption,
    onSelect: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(selected.label, fontSize = 13.sp, color = Color(0xFF6B6B76))
            Spacer(Modifier.width(4.dp))
            Text("▾", fontSize = 12.sp, color = Color(0xFF6B6B76))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 카테고리 필터 칩 하나
@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PassPurple else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF6B6B76),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// 질문 카드 하나
@Composable
private fun QuestionCard(
    question: QuestionSearchResponse,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 카테고리 뱃지
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PassPurpleLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = question.mainCategory,
                        color = PassPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (isBookmarked) "⭐" else "☆",
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onToggleBookmark() }
                )
            }

            Spacer(Modifier.height(10.dp))

            // 제목
            Text(
                text = question.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B33)
            )

            Spacer(Modifier.height(12.dp))

            // 태그들 (캡슐)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                question.tags.take(4).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TagBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("#$tag", color = TagText, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}