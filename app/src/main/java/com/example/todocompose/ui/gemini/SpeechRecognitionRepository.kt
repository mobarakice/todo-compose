package com.example.todocompose.ui.gemini

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionService
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
    private var recognizer: SpeechRecognizer? = null

    init {
        getAvailableVoiceRecognitionService(context)?.let {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context, it)
//                ComponentName(
//                    "com.google.android.as",
//                    "com.google.android.apps.miphone.aiai.app.AiAiSpeechRecognitionService"
//                ))
        }

    }

//    private val recognizer: SpeechRecognizer by lazy {
//        SpeechRecognizer.createSpeechRecognizer(context)
//    }

    override suspend fun startListening(onResult: (SpeechResult) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.apply {
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
                Log.i("startListening", "onBufferReceived")
            }

            override fun onReadyForSpeech(p0: Bundle?) {
                Log.i("startListening", "onReadyForSpeech")
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
                Log.i("startListening", "onPartialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        recognizer?.setRecognitionListener(recognitionListener)
        recognizer?.startListening(intent)
    }

    override suspend fun stopListening() {
        recognizer?.stopListening()
    }

    private fun getAvailableVoiceRecognitionService(activity: Context): ComponentName? {
        val services = activity.packageManager.queryIntentServices(
            Intent(RecognitionService.SERVICE_INTERFACE), 0
        )
        for (info in services) {
            val packageName = info.serviceInfo.packageName
            val serviceName = info.serviceInfo.name
            return ComponentName(
                packageName,
                serviceName
            )
        }
        return null
    }
}
