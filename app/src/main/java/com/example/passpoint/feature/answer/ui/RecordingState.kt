package com.example.passpoint.feature.answer.ui

import java.io.File

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(val elapsedSeconds: Int = 0) : RecordingState()
    data class Recorded(val file: File) : RecordingState()
    data class Playing(val file: File) : RecordingState()
}
