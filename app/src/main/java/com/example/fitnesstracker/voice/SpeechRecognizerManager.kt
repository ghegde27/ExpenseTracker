package com.example.fitnesstracker.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.fitnesstracker.logging.AppLogger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SpeechRecognizerManager(private val context: Context, private val logger: AppLogger) {
    fun listen(): Flow<Result<String>> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) { trySend(Result.failure(IllegalStateException("Speech recognition is unavailable"))); close(); return@callbackFlow }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) { val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull(); if (text != null) trySend(Result.success(text)); else trySend(Result.failure(IllegalStateException("No speech recognized"))); close() }
            override fun onError(error: Int) { logger.w("Voice", "Speech recognition failed: $error"); trySend(Result.failure(IllegalStateException("Speech recognition failed ($error)"))); close() }
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1) }
        logger.i("Voice", "Speech recognition started")
        recognizer.startListening(intent)
        awaitClose { recognizer.destroy() }
    }
}
