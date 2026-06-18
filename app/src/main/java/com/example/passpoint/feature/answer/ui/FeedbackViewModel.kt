package com.example.passpoint.feature.answer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.core.network.toUserMessage
import com.example.passpoint.feature.answer.data.AnswerRepository
import com.example.passpoint.feature.bookmark.data.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {

    private val repository = AnswerRepository()
    private val bookmarkRepository = BookmarkRepository()

    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Loading)
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    // 백엔드가 답변 상세 응답에 즐겨찾기 여부를 안 내려줘서 항상 비어있는 별로 시작한다.
    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()
    private var isBookmarkRequestInFlight = false

    private var loadedId: Long? = null

    fun load(answerId: Long) {
        if (loadedId == answerId) return
        loadedId = answerId
        _isBookmarked.value = false

        viewModelScope.launch {
            _uiState.value = FeedbackUiState.Loading
            try {
                val detail = repository.getAnswerDetail(answerId)
                _uiState.value = FeedbackUiState.Success(detail)
            } catch (e: Exception) {
                loadedId = null
                _uiState.value = FeedbackUiState.Error(e.toUserMessage("피드백을 불러올 수 없어요."))
            }
        }
    }

    /**
     * "다시 시도" 버튼에서 같은 answerId를 다시 불러올 수 있게 한다.
     */
    fun reload(answerId: Long) {
        loadedId = null
        load(answerId)
    }

    /**
     * 별 탭 → 먼저 화면을 바꾸고(낙관적 업데이트), 서버 요청이 실패하면 되돌린다.
     */
    fun toggleBookmark(questionId: Long) {
        if (isBookmarkRequestInFlight) return
        val nextValue = !_isBookmarked.value
        _isBookmarked.value = nextValue

        viewModelScope.launch {
            isBookmarkRequestInFlight = true
            try {
                if (nextValue) bookmarkRepository.add(questionId) else bookmarkRepository.remove(questionId)
            } catch (e: Exception) {
                _isBookmarked.value = !nextValue
            } finally {
                isBookmarkRequestInFlight = false
            }
        }
    }
}
