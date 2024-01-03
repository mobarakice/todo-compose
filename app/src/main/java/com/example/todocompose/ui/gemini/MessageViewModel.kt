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
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.MessageType
import com.example.todocompose.data.db.entity.ChatMessage
import com.example.todocompose.utils.WhileUiSubscribed
import com.example.todocompose.utils.toMessages
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
    val permissionDialog: Pair<Boolean, Int?> = Pair(false, null),
    val state: MessageState = MessageState.MessageTypeText.Typing
)

sealed class Prompt {
    data class ChatPrompt(val text: String) : Prompt()
    data class StructuredPrompt(val text: String) : Prompt()
}

sealed interface MessageState {

    sealed class MessageTypeText : MessageState {
        data object Loading : MessageTypeText()
        data object Typing : MessageTypeText()
    }

    sealed class MessageTypeAudio : MessageState {
        data object Loading : MessageTypeAudio()
        data object Listening : MessageTypeAudio()
        data object Speaking : MessageTypeAudio()
    }
}

class MessageViewModel(
    private val repository: AppRepository,
    private val geminiRepository: GeminiRepository = GeminiUseCase(),
    private val recognitionRepository: SpeechRecognitionRepository,
    private val ttsRepository: TTSRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ttsListener = object : TTSListener {
        override fun onStart(utteranceId: String?) {

        }

        override fun onDone(utteranceId: String?) {
            ttsRepository.stop()
            updateState(MessageState.MessageTypeAudio.Listening)
            startListening()
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            ttsRepository.stop()
            updateState(MessageState.MessageTypeAudio.Listening)
        }

    }

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
    private val _state: MutableStateFlow<MessageState> =
        MutableStateFlow(MessageState.MessageTypeText.Typing)

    private val _messages =
        repository.getChatRepository()
            .observeChatMessages()
            .map { it.toMessages() }

    val uiState: StateFlow<ChatUiState> = combine(
        _messages,
        _isLoading,
        _combinedStringFlow,
        _permissionDialog,
        _state
    ) { messages, isLoading, combinedStringFlow, permissionDialog, state ->
        ChatUiState(
            messages,
            isLoading,
            combinedStringFlow.first,
            combinedStringFlow.second,
            permissionDialog,
            state
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = ChatUiState(isLoading = true)
    )

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
                if (isRecognitionAvailable(context)) {
                    startListening()
                } else {
                    Log.i(TAG, "SpeechRecognizer is not available")
                }
            } else {
                val message = _userInputMessage.value
                updateUserMessage("")
                updateState(MessageState.MessageTypeText.Loading)
                createNewChatMessage(message, MessageType.SEND)
                callGeminiPro(Prompt.StructuredPrompt(message))
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
            when (val response = geminiRepository.startConversation(prompt)) {
                is GeminiResponse.Success -> {
                    createNewChatMessage(response.text, MessageType.RECEIVE)
                    when (_state.value) {
                        is MessageState.MessageTypeText -> {
                            updateState(MessageState.MessageTypeText.Typing)
                        }

                        is MessageState.MessageTypeAudio -> {
                            updateState(MessageState.MessageTypeAudio.Speaking)
                            ttsRepository.speak(response.text, ttsListener)
                        }
                    }
                }

                is GeminiResponse.Error -> {
                    Log.e(TAG, response.error)
                    when (_state.value) {
                        is MessageState.MessageTypeText ->
                            updateState(MessageState.MessageTypeText.Typing)

                        is MessageState.MessageTypeAudio ->
                            updateState(MessageState.MessageTypeAudio.Listening)

                    }
                }
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

    private fun updateState(state: MessageState) {
        _state.value = state
    }

    companion object {
        private val TAG = MessageViewModel::class.java.simpleName

        fun provideFactory(
            repository: AppRepository,
            geminiRepository: GeminiRepository,
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
                        repository,
                        geminiRepository,
                        recognitionRepository,
                        ttsRepository,
                        handle
                    ) as T
                }
            }
    }

    private fun startListening() {
        viewModelScope.launch {
            updateState(MessageState.MessageTypeAudio.Listening)
            Log.i(TAG, "SpeechRecognizer is available")
            recognitionRepository.startListening { result ->
                Log.i(TAG, "Text: ${result.text}")
                //updateUserMessage(result.text)
                viewModelScope.launch {
                    val text = result.text
                    createNewChatMessage(text, MessageType.SEND)
                    updateState(MessageState.MessageTypeAudio.Loading)
                    recognitionRepository.stopListening()
                    callGeminiPro(Prompt.ChatPrompt(text))
                }
            }
        }
    }

    private fun isRecognitionAvailable(context: Context) =
        SpeechRecognizer.isRecognitionAvailable(context)

    fun stopListening() {
        updateState(MessageState.MessageTypeText.Typing)
        viewModelScope.launch {
            recognitionRepository.stopListening()
            ttsRepository.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsRepository.stop()
        ttsRepository.shutdown()
    }
}

// Used to save USER last input in SavedStateHandle.
const val USER_LAST_INPUT_STATE_KEY = "user-last-input"