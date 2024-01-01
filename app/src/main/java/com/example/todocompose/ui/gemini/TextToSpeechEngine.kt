package com.example.todocompose.ui.gemini

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

interface TTSRepository {
    fun speak(text: String, listener: TTSListener)
    fun stop()

    fun shutdown()
}

interface TTSListener {
    fun onStart(utteranceId: String?)
    fun onDone(utteranceId: String?)
    fun onError(utteranceId: String?, errorCode: Int)
}

class TextToSpeechEngine(context: Context) : TTSRepository,
    UtteranceProgressListener() {

    private var listener: TTSListener? = null
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialization successful
                tts.apply {
                    language = Locale.ENGLISH
                    setPitch(1.0f)
                    setSpeechRate(1f)
                    setOnUtteranceProgressListener(this@TextToSpeechEngine)
                }
                Log.i("TextToSpeechEngine", "Initialization successful")

            } else {
                // Handle initialization error
                Log.i("TextToSpeechEngine", "Initialization error")

            }
        }
    }

    override fun speak(text: String, listener: TTSListener) {
        this.listener = listener
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, KEY_PARAM_UTTERANCE_ID)
    }

    override fun stop() {
        tts.stop()
    }

    override fun shutdown() {
        tts.shutdown()
    }

    override fun onStart(utteranceId: String?) {
        Log.i(TAG, "onStart() utteranceId:$utteranceId")
        listener?.onStart(utteranceId)
    }

    override fun onDone(utteranceId: String?) {
        Log.i(TAG, "onDone() utteranceId:$utteranceId")
        listener?.onDone(utteranceId)

    }

    @Deprecated("Deprecated in Java")
    override fun onError(utteranceId: String?) {
        Log.i(TAG, "onError() utteranceId:$utteranceId")
        listener?.onError(utteranceId, -1)
    }

    override fun onError(utteranceId: String?, errorCode: Int) {
        Log.i(TAG, "onError() utteranceId:$utteranceId error code:$errorCode")
        listener?.onError(utteranceId, errorCode)
    }

    companion object {
        private const val TAG = "TextToSpeechEngine"
    }
}
