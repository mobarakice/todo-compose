package com.example.todocompose.ui.gemini

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.MessageType
import com.example.todocompose.data.db.entity.ChatMessage
import com.example.todocompose.utils.Result
import com.example.todocompose.utils.WhileUiSubscribed
import com.example.todocompose.utils.toMessages
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String = ""
)

class MessageViewModel(
    private val repository: AppRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _userMessage: MutableStateFlow<String> = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)

    private val _messages =
        repository.getChatRepository()
            .observeChatMessages()
            .map { Result.Success(it.toMessages()) }

    val uiState: StateFlow<ChatUiState> = combine(
        _messages, _isLoading, _userMessage
    ) { messages, isLoading, userMessage ->
        ChatUiState(
            messages.data, isLoading, userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = ChatUiState(isLoading = true)
    )

    fun updateMessage(newMessage: String) {
        _userMessage.update { message -> newMessage }
    }

    fun sendNewMessage() {
        if (uiState.value.userMessage.isEmpty()) {
            return
        }

        createNewChatMessage(_userMessage.value, MessageType.SEND)
        callGeminiPro(_userMessage.value)
    }

    private fun createNewChatMessage(message: String, messageType: MessageType) {
        viewModelScope.launch {
            repository.getChatRepository().insertChatMessage(
                ChatMessage(message = message, messageType = messageType)
            )
        }
    }

    private fun callGeminiPro(message: String) {
        viewModelScope.launch {
            try {
                val model = GenerativeModel(
                    "gemini-pro",
                    // Retrieve API key as an environmental variable defined in a Build Configuration
                    // see https://github.com/google/secrets-gradle-plugin for further instructions
                    System.getenv("apiKey")?:"",
                    generationConfig = generationConfig {
                        temperature = 0.9f
                        topK = 1
                        topP = 1f
                        maxOutputTokens = 2048
                    },
                    safetySettings = listOf(
                        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                        SafetySetting(
                            HarmCategory.SEXUALLY_EXPLICIT,
                            BlockThreshold.MEDIUM_AND_ABOVE
                        ),
                        SafetySetting(
                            HarmCategory.DANGEROUS_CONTENT,
                            BlockThreshold.MEDIUM_AND_ABOVE
                        ),
                    ),
                )

                val chatHistory = listOf(
                    content("user") {
                        text("a = 5, b = 10")
                    },
                    content("model") {
                        text("15")
                    },
                    content("user") {
                        text("a = 7, b = 9")
                    },
                    content("model") {
                        text("16")
                    },
                )

                val chat = model.startChat(chatHistory)
                val response = chat.sendMessage(message)
                Log.i(TAG, "Response: $response")
                val text = response.text ?: ""
                Log.i(TAG, "Text: $text")
                val contentParts = response.candidates.first().content.parts.first().asTextOrNull()
                Log.i(TAG, "Total: $contentParts")

                createNewChatMessage(text, MessageType.RECEIVE)
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
            }
        }
    }

    companion object {
        private val TAG = MessageViewModel::class.java.simpleName

        fun provideFactory(
            repository: AppRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return MessageViewModel(repository, handle) as T
                }
            }
    }
}