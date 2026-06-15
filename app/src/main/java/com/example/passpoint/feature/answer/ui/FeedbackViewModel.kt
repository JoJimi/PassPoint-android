package com.example.passpoint.feature.answer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.core.network.toUserMessage
import com.example.passpoint.feature.answer.data.AnswerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {

    private val repository = AnswerRepository()

    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Loading)
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private var loadedId: Long? = null

    fun load(answerId: Long) {
        if (loadedId == answerId) return
        loadedId = answerId

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
}
