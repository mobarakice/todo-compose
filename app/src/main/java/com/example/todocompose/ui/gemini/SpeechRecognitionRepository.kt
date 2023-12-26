package com.example.todocompose.ui.gemini

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

interface SpeechRecognitionRepository {
    suspend fun startListening(onResult: (SpeechResult) -> Unit)
    suspend fun stopListening()
}

data class SpeechResult(val text: String, val timestamp: Long)

class SpeechRecognitionUseCase(private val context: Context) : SpeechRecognitionRepository {

    private val recognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    override suspend fun startListening(onResult: (SpeechResult) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.apply{
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en",
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault(),
            )
        }
        val recognitionListener = object : RecognitionListener {
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {
                Log.i("startListening","onBufferReceived")
            }

            override fun onReadyForSpeech(p0: Bundle?) {
                Log.i("startListening","onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onResults(results: Bundle) {
                val matchedText = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (matchedText != null) {
                    onResult(SpeechResult(matchedText, System.currentTimeMillis()))
                }
            }

            override fun onError(error: Int) {}

            override fun onPartialResults(p0: Bundle?) {
                Log.i("startListening","onPartialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        recognizer.setRecognitionListener(recognitionListener)
        recognizer.startListening(intent)
    }

    override suspend fun stopListening() {
        recognizer.stopListening()
    }
}
