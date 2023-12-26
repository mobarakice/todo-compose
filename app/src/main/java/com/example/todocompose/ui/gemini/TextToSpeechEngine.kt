package com.example.todocompose.ui.gemini

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
import android.speech.tts.UtteranceProgressListener
import android.util.Log

interface TTSRepository {
    fun speak(text: String)
    fun stop()
}

class TextToSpeechEngine(private val context: Context) : TTSRepository {

    private val tts: TextToSpeech by lazy {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialization successful
                Log.i("TextToSpeechEngine", "Initialization successful")

            } else {
                // Handle initialization error
                Log.i("TextToSpeechEngine", "Initialization error")

            }
        }
    }


    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.i("TextToSpeechEngine", "setOnUtteranceProgressListener#start")
            }

            override fun onDone(utteranceId: String?) {
                Log.i("TextToSpeechEngine", "setOnUtteranceProgressListener#onDone")
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.i("TextToSpeechEngine", "setOnUtteranceProgressListener#onError")
            }

        })
    }

    override fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, KEY_PARAM_UTTERANCE_ID)
    }

    override fun stop() {
        tts.shutdown()
    }
}
