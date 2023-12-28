package com.example.todocompose.ui.gemini

import android.content.Context
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.BuildConfig
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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
    val permissionDialog: Pair<Boolean, Int?> = Pair(false, null)
)

sealed interface Prompt
data class ChatPrompt(val text: String) : Prompt
data class StructuredPrompt(val text: String) : Prompt

class MessageViewModel(
    private val recognitionRepository: SpeechRecognitionRepository,
    private val ttsRepository: TTSRepository,
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
    private val _permissionDialog: MutableStateFlow<Pair<Boolean, Int?>> =
        MutableStateFlow(Pair(false, null))
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

    fun sendNewMessage(context: Context) {
        viewModelScope.launch {

            if (_userInputMessage.value.isEmpty()) {
                //updateErrorMessage(R.string.ask_your_question_here)
                startListening(context)
            } else {
                val message = _userInputMessage.value
                updateUserMessage("")
                createNewChatMessage(message, MessageType.SEND)
                callGeminiPro(StructuredPrompt(message))
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

    private suspend fun callGeminiPro(prompt: Prompt) {
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
                val response = sendMessage(prompt, model)
                Log.i(TAG, "Response: $response")
                val text = response.text ?: ""
                Log.i(TAG, "Text: $text")
                val contentParts = response.candidates.first().content.parts.first().asTextOrNull()
                Log.i(TAG, "Total: $contentParts")

                createNewChatMessage(text, MessageType.RECEIVE)
                ttsRepository.speak(text)
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
            }
        }
    }

    private suspend fun sendMessage(prompt: Prompt, model: GenerativeModel) = when (prompt) {
        is ChatPrompt -> {
            model.startChat(getChatPrompt())
                .sendMessage(prompt.text)
        }

        is StructuredPrompt -> {
            model.generateContent(getStructuredPrompt(prompt.text))
        }
    }

    private fun getStructuredPrompt(text: String) = content {
        text("English trainer, refine my advanced English! <div>-Correct & explain my mistakes. </div><div>-Offer advanced alternatives. </div><div>-Output a maximum of 80 words</div>")
        text("Request: Hello, how are you?")
        text("Response: Your sentence is grammatically correct! However, depending on the context and desired formality, here are some advanced alternatives:\n\"Good morning/afternoon/evening, I hope you are well.\"\n\"Great to see you! How's it going?\"\n\"Hello! Are you tackling any exciting projects nowadays?\"")
        text("Request: What are you done?")
        text("Response: \"What are you done?\" is grammatically incorrect. The correct way to ask would be \"What have you done?\" or \"What are you doing?\" depending on the context and time frame.\nAlternatives in advanced English:\n-\"What have you accomplished recently?\" \n-\"What have you been up to lately?\" \n-\"What progress have you made on [project/task]?\" (Tailored to a specific context).")
        text("Request: $text")
        text("Response: ")
    }

    private fun getChatPrompt() = listOf(
        content("user") {
            text(
                """English trainer, refine my advanced English!
                    |-Correct & explain my mistakes.
                    |-Offer advanced alternatives.
                    |-Output a maximum of 80 words.
                |Hello, how are you?""".trimMargin("|")
            )
        },
        content("model") {
            text(
                """Your sentence is grammatically correct! However, depending on the context and desired formality,
                |here are some advanced alternatives:
                |"Good morning/afternoon/evening, I hope you are well."
                |"Great to see you! How's it going?"
                |"Hello! Are you tackling any exciting projects nowadays?"""".trimMargin("|")
            )
        },
        content("user") {
            text(
                """English trainer, refine my advanced English!
                    |-Correct & explain my mistakes.
                    |-Offer advanced alternatives.
                    |-Output a maximum of 80 words.
                |What are you done?""".trimMargin("|")
            )
        },
        content("model") {
            text(
                """"What are you done?" is grammatically incorrect. The correct way to ask would be "What have you done?" or "What are you doing?" depending on the context and time frame.
                |Alternatives in advanced English:
                |"What have you accomplished recently?
                |"What have you been up to lately?"
                |"What progress have you made on [project/task]?" (Tailored to a specific context)"
                """.trimMargin("|")
            )
        }
    )

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
            recognitionRepository: SpeechRecognitionRepository,
            ttsRepository: TTSRepository,
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
                    return MessageViewModel(
                        recognitionRepository,
                        ttsRepository,
                        repository,
                        handle
                    ) as T
                }
            }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private fun startListening(context: Context) {
//        scope.launch {
//
//        }
        viewModelScope.launch {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.i(TAG, "SpeechRecognizer is available")
                recognitionRepository.startListening { result ->
                    Log.i(TAG, "Text: ${result.text}")
                    updateUserMessage(result.text)
                }
            } else {
                Log.i(TAG, "SpeechRecognizer is not available")
            }
        }
    }

    fun stopListening() {
        scope.launch {
            recognitionRepository.stopListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

// Used to save USER last input in SavedStateHandle.
const val USER_LAST_INPUT_STATE_KEY = "user-last-input"