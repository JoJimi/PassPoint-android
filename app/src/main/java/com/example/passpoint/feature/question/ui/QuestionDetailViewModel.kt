package com.example.passpoint.feature.question.ui

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.core.network.toUserMessage
import com.example.passpoint.feature.answer.data.AnswerRepository
import com.example.passpoint.feature.answer.ui.AnswerSubmitState
import com.example.passpoint.feature.answer.ui.RecordingState
import com.example.passpoint.feature.question.data.QuestionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

private const val ANSWER_MAX_LENGTH = 3000

class QuestionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository()
    private val answerRepository = AnswerRepository()

    private val _uiState = MutableStateFlow<QuestionDetailUiState>(QuestionDetailUiState.Loading)
    val uiState: StateFlow<QuestionDetailUiState> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<AnswerSubmitState>(AnswerSubmitState.Idle)
    val submitState: StateFlow<AnswerSubmitState> = _submitState.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private var loadedId: Long? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordedFile: File? = null
    private var timerJob: Job? = null
    private var uploadedAudioKey: String? = null

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
     * F7: presigned URL 발급 → MinIO PUT 업로드 → audioKey 보관
     * F8에서 POST /answers(VOICE) 호출 + 처리중 화면 연결 추가 예정
     */
    fun submitVoiceAnswer(questionId: Long) {
        val file = recordedFile ?: run {
            _submitState.value = AnswerSubmitState.Error("녹음된 파일이 없어요.")
            return
        }

        viewModelScope.launch {
            _submitState.value = AnswerSubmitState.Submitting
            try {
                val presigned = answerRepository.getAudioPresignedUrl()
                answerRepository.uploadAudio(presigned.uploadUrl, file)
                uploadedAudioKey = presigned.key
                // F8에서 여기에 POST /answers(VOICE, audioKey) + 처리중 화면 네비게이션 추가
                _submitState.value = AnswerSubmitState.Error("음성 업로드 완료! (F8에서 제출 연결 예정)")
            } catch (e: Exception) {
                _submitState.value = AnswerSubmitState.Error(e.toUserMessage("음성 업로드에 실패했어요."))
            }
        }
    }

    fun consumeSubmitState() {
        _submitState.value = AnswerSubmitState.Idle
    }

    fun startRecording() {
        val cacheDir = getApplication<Application>().cacheDir
        val outputFile = File(cacheDir, "voice_answer_${System.currentTimeMillis()}.m4a")

        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(getApplication())
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        try {
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            recordedFile = outputFile
            _recordingState.value = RecordingState.Recording(0)
            startTimer()
        } catch (e: Exception) {
            mediaRecorder.release()
            _submitState.value = AnswerSubmitState.Error("녹음을 시작할 수 없어요: ${e.message}")
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(1000)
                seconds++
                _recordingState.value = RecordingState.Recording(seconds)
            }
        }
    }

    fun stopRecording() {
        timerJob?.cancel()
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) { }
        recorder = null

        val file = recordedFile
        _recordingState.value = if (file != null && file.exists()) {
            RecordingState.Recorded(file)
        } else {
            RecordingState.Idle
        }
    }

    fun startPlayback() {
        val file = recordedFile ?: return
        try {
            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    _recordingState.value = RecordingState.Recorded(file)
                    release()
                    player = null
                }
                prepare()
                start()
            }
            _recordingState.value = RecordingState.Playing(file)
        } catch (_: Exception) {
            _recordingState.value = RecordingState.Recorded(file)
        }
    }

    fun stopPlayback() {
        val file = recordedFile
        try {
            player?.apply {
                stop()
                release()
            }
        } catch (_: Exception) { }
        player = null
        _recordingState.value = if (file != null) RecordingState.Recorded(file) else RecordingState.Idle
    }

    fun resetRecording() {
        timerJob?.cancel()
        try { player?.apply { stop(); release() } } catch (_: Exception) { }
        player = null
        try { recorder?.apply { stop(); release() } } catch (_: Exception) { }
        recorder = null
        recordedFile?.delete()
        recordedFile = null
        _recordingState.value = RecordingState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        try { recorder?.apply { stop(); release() } } catch (_: Exception) { }
        try { player?.apply { stop(); release() } } catch (_: Exception) { }
        recorder = null
        player = null
    }
}
