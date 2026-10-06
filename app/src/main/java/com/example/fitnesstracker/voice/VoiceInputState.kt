package com.example.fitnesstracker.voice

sealed interface VoiceInputState {
    data object Idle : VoiceInputState
    data object Listening : VoiceInputState
    data object Processing : VoiceInputState
    data class Result(
        val text: String,
        val suggestion: VoiceTransaction
    ) : VoiceInputState
    data class Error(
        val message: String
    ) : VoiceInputState
}
