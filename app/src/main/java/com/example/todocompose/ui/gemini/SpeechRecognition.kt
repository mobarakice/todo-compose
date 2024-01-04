package com.example.todocompose.ui.gemini

import android.content.Context
import android.util.Log
import org.deepspeech.libdeepspeech.DeepSpeechModel
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SpeechRecognition(context: Context) : SpeechRecognitionRepository {
    private var _model: DeepSpeechModel
    private val BEAM_WIDTH = 50L
    private val appAudioRecorder: AppAudioRecorder

    init {
        appAudioRecorder = AppAudioRecorder(context)
        _model = DeepSpeechModel(getModelPath(context)).apply {
            setBeamWidth(BEAM_WIDTH)
        }

    }

    fun getModelPath(context: Context) = File(
        context.cacheDir,
        "deepspeech-0.9.3-models.tflite"
    ).absolutePath

    fun stt(bytes: ByteArray): String {
        val shorts = ShortArray(bytes.size / 2)
        // to turn bytes to shorts as either big endian or little endian.
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]
        val decoded: String = _model.stt(shorts, shorts.size) ?: ""
        Log.i("TAG", "Decode: $decoded")
        return decoded
    }

    fun destroy() {
        _model.freeModel()
    }

    override suspend fun startListening(onResult: (SpeechResult) -> Unit) {
        appAudioRecorder.startRecording()
        val string = stt(appAudioRecorder.data.value)
        Log.i("SpeechRecognition","text $string")
        onResult(SpeechResult(string, System.currentTimeMillis()))
    }

    override suspend fun stopListening() {
        destroy()
    }

}
