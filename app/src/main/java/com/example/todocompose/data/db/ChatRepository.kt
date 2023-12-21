package com.example.todocompose.data.db

import com.example.todocompose.data.db.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * This is repository class for database and handle all kind of database operation through this
 * @author Mobarak
 */
interface ChatRepository {
    fun observeChatMessages(): Flow<List<ChatMessage>>
    fun observeChatMessageById(messageId: Long): Flow<ChatMessage>
    suspend fun getChatMessages(): List<ChatMessage>
    suspend fun getChatMessageById(messageId: Long): ChatMessage?
    suspend fun insertChatMessage(message: ChatMessage)
    suspend fun updateChatMessage(message: ChatMessage): Int
    suspend fun deleteChatMessageById(messageId: Long): Int
    suspend fun deleteChatMessages()
}