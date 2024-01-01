package com.example.todocompose.ui.gemini

import android.util.Log
import com.example.todocompose.BuildConfig
import com.example.todocompose.ui.gemini.GeminiUtils.MAX_OUTPUT_TOKENS
import com.example.todocompose.ui.gemini.GeminiUtils.TEMPERATURE
import com.example.todocompose.ui.gemini.GeminiUtils.TOP_K
import com.example.todocompose.ui.gemini.GeminiUtils.TOP_P
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface GeminiRepository {
    suspend fun startConversation(prompt: Prompt): ResponseType
}

sealed interface ResponseType
sealed class GeminiResponse : ResponseType {
    data class Success(val text: String) : GeminiResponse()
    data class Error(val error: String) : GeminiResponse()
}


class GeminiUseCase : GeminiRepository {
    private val model: GenerativeModel

    init {
        model = GenerativeModel(
            GeminiUtils.MODEL_GEMINI_PRO,
            BuildConfig.apiKey,
            generationConfig = generationConfig {
                temperature = TEMPERATURE
                topK = TOP_K
                topP = TOP_P
                maxOutputTokens = MAX_OUTPUT_TOKENS
            },
            safetySettings = GeminiUtils.getSafetySettings()
        )
    }

    private suspend fun sendMessage(prompt: Prompt) = when (prompt) {
        is Prompt.ChatPrompt -> {
            model.startChat(GeminiUtils.getChatPrompt())
                .sendMessage(prompt.text)
        }

        is Prompt.StructuredPrompt -> {
            model.generateContent(GeminiUtils.getStructuredPrompt(prompt.text))
        }
    }

    override suspend fun startConversation(prompt: Prompt): ResponseType =
        withContext(Dispatchers.IO) {
            try {
                val response = sendMessage(prompt)
                Log.i(TAG, "Response: $response")
                val text = response.text ?: ""
                Log.i(TAG, "Text: $text")
                val contentParts = response.candidates.first().content.parts.first().asTextOrNull()
                Log.i(TAG, "Total: $contentParts")
                GeminiResponse.Success(contentParts ?: "")
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
                GeminiResponse.Error(e.message ?: "")
            }
        }

    companion object {
        const val TAG = "GeminiUseCase"
    }
}