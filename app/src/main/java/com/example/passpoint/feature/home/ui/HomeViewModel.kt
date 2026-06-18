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

    // init에서 한 번만 부르면 마이페이지에서 닉네임을 바꾼 뒤 홈 탭으로 돌아와도
    // 살아있는 같은 ViewModel 인스턴스라 새로 안 불러온다. 화면에 들어올 때마다
    // refresh()를 호출하게 하고(HomeScreen의 LaunchedEffect), 여기서는 자동 로드를 하지 않는다.

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
