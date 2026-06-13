package com.example.passpoint.feature.question.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.feature.question.data.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionDetailViewModel : ViewModel() {

    private val repository = QuestionRepository()

    private val _uiState = MutableStateFlow<QuestionDetailUiState>(QuestionDetailUiState.Loading)
    val uiState: StateFlow<QuestionDetailUiState> = _uiState.asStateFlow()

    // 같은 id를 다시 요청하지 않기 위해 마지막으로 불러온 id를 기억한다.
    private var loadedId: Long? = null

    fun load(id: Long) {
        if (loadedId == id) return
        loadedId = id

        viewModelScope.launch {
            _uiState.value = QuestionDetailUiState.Loading
            try {
                val question = repository.getDetail(id)
                _uiState.value = QuestionDetailUiState.Success(question)
            } catch (e: Exception) {
                loadedId = null
                _uiState.value = QuestionDetailUiState.Error(e.message ?: "불러오기 실패")
            }
        }
    }
}
