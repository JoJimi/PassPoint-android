package com.example.passpoint.feature.question.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.feature.question.data.QuestionRepository
import com.example.passpoint.feature.question.data.dto.response.QuestionSearchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionListViewModel : ViewModel() {

    private val repository = QuestionRepository()

    private val _uiState = MutableStateFlow<QuestionListUiState>(QuestionListUiState.Loading)
    val uiState: StateFlow<QuestionListUiState> = _uiState.asStateFlow()

    // 현재 검색 조건 (페이징 동안 유지)
    private var currentKeyword: String? = null
    private var currentCategory: String? = null

    // 페이징 상태
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false          // 중복 요청 방지

    // 지금까지 쌓인 목록
    private val loadedQuestions = mutableListOf<QuestionSearchResponse>()

    init {
        loadFirstPage()
    }

    /**
     * 검색 조건이 바뀌었을 때 (칩 변경, 키워드 검색).
     * page 0부터 완전히 새로 시작한다.
     */
    fun search(keyword: String? = null, category: String? = null) {
        currentKeyword = keyword
        currentCategory = category
        loadFirstPage()
    }

    /**
     * 첫 페이지 로드 (또는 조건 변경 시 초기화).
     */
    private fun loadFirstPage() {
        currentPage = 0
        isLastPage = false
        loadedQuestions.clear()
        _uiState.value = QuestionListUiState.Loading

        viewModelScope.launch {
            isLoading = true
            try {
                val pageData = repository.search(
                    keyword = currentKeyword,
                    category = currentCategory,
                    page = 0
                )
                loadedQuestions.addAll(pageData.content)
                isLastPage = pageData.last
                _uiState.value = QuestionListUiState.Success(
                    questions = loadedQuestions.toList(),
                    totalCount = pageData.totalElements,
                    isLastPage = isLastPage
                )
            } catch (e: Exception) {
                _uiState.value = QuestionListUiState.Error(e.message ?: "불러오기 실패")
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 다음 페이지 로드 (스크롤이 끝에 닿았을 때 호출).
     */
    fun loadNextPage() {
        // 이미 로딩 중이거나, 마지막 페이지면 무시
        if (isLoading || isLastPage) return

        viewModelScope.launch {
            isLoading = true
            // 기존 목록 유지하면서 "더 불러오는 중" 표시
            _uiState.value = QuestionListUiState.Success(
                questions = loadedQuestions.toList(),
                isLoadingMore = true,
                isLastPage = isLastPage
            )
            try {
                val nextPage = currentPage + 1
                val pageData = repository.search(
                    keyword = currentKeyword,
                    category = currentCategory,
                    page = nextPage
                )
                loadedQuestions.addAll(pageData.content)
                currentPage = nextPage
                isLastPage = pageData.last
                _uiState.value = QuestionListUiState.Success(
                    questions = loadedQuestions.toList(),
                    isLoadingMore = false,
                    isLastPage = isLastPage
                )
            } catch (e: Exception) {
                // 다음 페이지 실패는 기존 목록 유지 + 로딩만 끔
                _uiState.value = QuestionListUiState.Success(
                    questions = loadedQuestions.toList(),
                    isLoadingMore = false,
                    isLastPage = isLastPage
                )
            } finally {
                isLoading = false
            }
        }
    }
}