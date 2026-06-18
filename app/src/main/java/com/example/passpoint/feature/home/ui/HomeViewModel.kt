package com.example.passpoint.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.feature.answer.data.AnswerRepository
import com.example.passpoint.feature.user.data.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val RECENT_ANSWERS_SIZE = 3

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val answerRepository = AnswerRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                coroutineScope {
                    val profileDeferred = async { userRepository.getMe() }
                    val statsDeferred = async { userRepository.getStats() }
                    val answersDeferred = async {
                        answerRepository.getAnswerList(page = 0, size = RECENT_ANSWERS_SIZE)
                    }
                    _uiState.value = HomeUiState.Success(
                        nickname = profileDeferred.await().nickname,
                        stats = statsDeferred.await(),
                        recentAnswers = answersDeferred.await().content
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "불러오기 실패")
            }
        }
    }

    fun refresh() = load()
}
