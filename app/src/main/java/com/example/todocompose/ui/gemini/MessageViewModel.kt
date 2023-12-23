package com.example.todocompose.ui.gemini

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.BuildConfig
import com.example.todocompose.R
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.MessageType
import com.example.todocompose.data.db.entity.ChatMessage
import com.example.todocompose.utils.WhileUiSubscribed
import com.example.todocompose.utils.toMessages
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String = "",
    val errorMessage: Int? = null,
    val permissionDialog: Pair<Boolean,Int?> = Pair(false,null)
)

class MessageViewModel(
    private val repository: AppRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedUserLastInput = savedStateHandle.getStateFlow(
        USER_LAST_INPUT_STATE_KEY, ""
    )
    private val _userInputMessage: MutableStateFlow<String> = MutableStateFlow("")
    private val _errorMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _combinedStringFlow = combine(
        _userInputMessage,
        _savedUserLastInput,
        _errorMessage
    ) { userInputMessage, savedLastInput, errorMessage ->
        val userMsg = savedLastInput.ifEmpty { userInputMessage }
        Pair(userMsg, errorMessage)
    }
    private val _permissionDialog: MutableStateFlow<Pair<Boolean, Int?>> = MutableStateFlow(Pair(false,null))
    private val _isLoading = MutableStateFlow(false)

    private val _messages =
        repository.getChatRepository()
            .observeChatMessages()
            .map { it.toMessages() }

    val uiState: StateFlow<ChatUiState> = combine(
        _messages,
        _isLoading,
        _combinedStringFlow,
        _permissionDialog
    ) { messages, isLoading, combinedStringFlow, permissionDialog ->
        ChatUiState(
            messages,
            isLoading,
            combinedStringFlow.first,
            combinedStringFlow.second,
            permissionDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = ChatUiState(isLoading = true)
    )

//    val uiState: StateFlow<ChatUiState>

    fun updateUserMessage(newMessage: String) {
        _userInputMessage.value = newMessage
        savedStateHandle[USER_LAST_INPUT_STATE_KEY] = newMessage
    }

    fun updateErrorMessage(newMessage: Int?) {
        _errorMessage.value = newMessage
    }

    fun sendNewMessage() {
        viewModelScope.launch {

            if (_userInputMessage.value.isEmpty()) {
                updateErrorMessage(R.string.ask_your_question_here)
            } else {
                val message = _userInputMessage.value
                updateUserMessage("")
                createNewChatMessage(message, MessageType.SEND)
                callGeminiPro(message)
            }
        }
    }

    private suspend fun createNewChatMessage(message: String, messageType: MessageType) {
        withContext(Dispatchers.IO) {
            repository.getChatRepository()
                .insertChatMessage(
                    ChatMessage(message = message, messageType = messageType)
                )
        }
    }

    private suspend fun callGeminiPro(message: String) {
        withContext(Dispatchers.IO) {
            try {
                val model = GenerativeModel(
                    "gemini-pro",
                    // Retrieve API key as an environmental variable defined in a Build Configuration
                    // see https://github.com/google/secrets-gradle-plugin for further instructions
                    BuildConfig.apiKey,
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
                        text("Fruits")
                    },
                    content("model") {
                        text("Apple, Mango, Orange")
                    }
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

    fun updatePermissionDialogState(isShowDialog: Boolean) {
        _permissionDialog.update {
            it.copy(first = isShowDialog)
        }
    }

    fun updateDialogText(text: Int) {
        _permissionDialog.update {
            it.copy(second = text)
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

// Used to save USER last input in SavedStateHandle.
const val USER_LAST_INPUT_STATE_KEY = "user-last-input"