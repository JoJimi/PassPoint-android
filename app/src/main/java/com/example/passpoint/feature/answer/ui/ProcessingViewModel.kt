package com.example.passpoint.feature.answer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.feature.answer.data.AnswerRepository
import com.example.passpoint.feature.answer.domain.AnswerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MAX_ATTEMPTS = 30
private const val POLL_INTERVAL_MS = 1500L

class ProcessingViewModel : ViewModel() {

    private val repository = AnswerRepository()

    private val _uiState = MutableStateFlow<ProcessingUiState>(ProcessingUiState.Loading)
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    // 같은 answerId로 다시 폴링을 시작하지 않기 위해 마지막으로 시작한 id를 기억한다.
    private var pollingAnswerId: Long? = null

    /**
     * GET /answers/{id}를 폴링해 DONE/FAILED가 나오면 멈춘다.
     * 2주차(동기 처리)에서는 첫 호출에서 바로 DONE/FAILED가 와서 폴링이 한 번만 돈다.
     * 3주차 비동기 전환 후에는 ANALYZING 등이 여러 번 돌다가 DONE이 되는 형태로 그대로 재사용된다.
     */
    fun startPolling(answerId: Long) {
        if (pollingAnswerId == answerId) return
        pollingAnswerId = answerId

        viewModelScope.launch {
            _uiState.value = ProcessingUiState.Loading
            repeat(MAX_ATTEMPTS) {
                try {
                    val detail = repository.getAnswerDetail(answerId)
                    when (AnswerStatus.from(detail.status)) {
                        AnswerStatus.DONE -> {
                            _uiState.value = ProcessingUiState.Done(answerId)
                            return@launch
                        }
                        AnswerStatus.FAILED -> {
                            _uiState.value = ProcessingUiState.Failed
                            return@launch
                        }
                        else -> delay(POLL_INTERVAL_MS)
                    }
                } catch (_: Exception) {
                    _uiState.value = ProcessingUiState.Failed
                    return@launch
                }
            }
            _uiState.value = ProcessingUiState.Timeout
        }
    }
}
