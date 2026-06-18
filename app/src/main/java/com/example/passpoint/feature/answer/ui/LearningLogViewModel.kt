package com.example.passpoint.feature.answer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.feature.answer.data.AnswerRepository
import com.example.passpoint.feature.answer.data.dto.response.AnswerSummaryResponse
import com.example.passpoint.feature.user.data.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LearningLogViewModel : ViewModel() {

    private val answerRepository = AnswerRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow<LearningLogUiState>(LearningLogUiState.Loading)
    val uiState: StateFlow<LearningLogUiState> = _uiState.asStateFlow()

    private val loadedAnswers = mutableListOf<AnswerSummaryResponse>()
    private var currentPage = 0
    private var currentCategory: String? = null
    private var isLastPage = false
    private var isLoading = false

    init {
        loadFirstPage()
    }

    private fun loadFirstPage() {
        currentPage = 0
        isLastPage = false
        loadedAnswers.clear()
        _uiState.value = LearningLogUiState.Loading

        viewModelScope.launch {
            isLoading = true
            try {
                coroutineScope {
                    val statsDeferred = async { userRepository.getStats() }
                    val listDeferred = async { answerRepository.getAnswerList(category = currentCategory, page = 0) }

                    val stats = statsDeferred.await()
                    val pageData = listDeferred.await()

                    loadedAnswers.addAll(pageData.content)
                    isLastPage = pageData.last
                    _uiState.value = LearningLogUiState.Success(
                        answers = loadedAnswers.toList(),
                        stats = stats,
                        isLastPage = isLastPage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = LearningLogUiState.Error(e.message ?: "불러오기 실패")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || isLastPage) return

        val currentState = _uiState.value as? LearningLogUiState.Success ?: return

        viewModelScope.launch {
            isLoading = true
            _uiState.value = currentState.copy(isLoadingMore = true)
            try {
                val nextPage = currentPage + 1
                val pageData = answerRepository.getAnswerList(category = currentCategory, page = nextPage)
                loadedAnswers.addAll(pageData.content)
                currentPage = nextPage
                isLastPage = pageData.last
                _uiState.value = currentState.copy(
                    answers = loadedAnswers.toList(),
                    isLoadingMore = false,
                    isLastPage = isLastPage
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(isLoadingMore = false)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 카테고리 칩 선택. null이면 전체, 값이 있으면 그 카테고리로 첫 페이지부터 다시 불러온다.
     */
    fun filterByCategory(category: String?) {
        if (currentCategory == category) return
        currentCategory = category
        loadFirstPage()
    }

    fun refresh() {
        loadFirstPage()
    }
}
