package com.example.todocompose.ui.gemini

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext


class AppAudioRecorder (val context: Context){

    private var recorder: AudioRecord? = null
    private var isRecording = false
    private val SAMPLE_RATE_IN_HZ = 16000
    private val _data: MutableStateFlow<ByteArray> = MutableStateFlow(ByteArray(0))
    val data: StateFlow<ByteArray> = _data
    val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE_IN_HZ,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    suspend fun startRecording() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        recorder?.let {
            it.startRecording()
            isRecording = true
            record(it, bufferSize)
        }
    }

    private suspend fun record(record: AudioRecord, bufferSize: Int) {
        withContext(Dispatchers.IO) {
            val buffer = ByteArray(bufferSize)
            while (isRecording) {
                val read = record.read(buffer, 0, bufferSize)
                if (read > 0) {
                    _data.value = buffer
                }
            }
        }
    }

    fun stopRecording() {
        isRecording = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}