package com.example.passpoint.feature.question.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.core.network.toUserMessage
import com.example.passpoint.feature.answer.data.AnswerRepository
import com.example.passpoint.feature.answer.ui.AnswerSubmitState
import com.example.passpoint.feature.question.data.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val ANSWER_MAX_LENGTH = 3000

class QuestionDetailViewModel : ViewModel() {

    private val repository = QuestionRepository()
    private val answerRepository = AnswerRepository()

    private val _uiState = MutableStateFlow<QuestionDetailUiState>(QuestionDetailUiState.Loading)
    val uiState: StateFlow<QuestionDetailUiState> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<AnswerSubmitState>(AnswerSubmitState.Idle)
    val submitState: StateFlow<AnswerSubmitState> = _submitState.asStateFlow()

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

    /**
     * 텍스트 답변 제출.
     * 빈 답변/3000자 초과는 서버 호출 전에 먼저 막는다 (서버도 같은 검증을 하지만 불필요한 호출 방지).
     */
    fun submitAnswer(questionId: Long, answerText: String) {
        if (answerText.isBlank()) {
            _submitState.value = AnswerSubmitState.Error("답변을 입력해주세요.")
            return
        }
        if (answerText.length > ANSWER_MAX_LENGTH) {
            _submitState.value = AnswerSubmitState.Error("답변은 ${ANSWER_MAX_LENGTH}자 이하로 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _submitState.value = AnswerSubmitState.Submitting
            try {
                val response = answerRepository.submitAnswer(questionId, answerText)
                _submitState.value = AnswerSubmitState.Success(response.answerId)
            } catch (e: Exception) {
                _submitState.value = AnswerSubmitState.Error(e.toUserMessage("답변 제출에 실패했어요."))
            }
        }
    }

    /**
     * Success/Error를 화면에서 1회 소비(네비게이션/토스트)한 뒤 Idle로 되돌린다.
     */
    fun consumeSubmitState() {
        _submitState.value = AnswerSubmitState.Idle
    }
}
