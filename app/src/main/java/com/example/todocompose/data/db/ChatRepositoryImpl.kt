package com.example.todocompose.data.db

import com.example.todocompose.data.db.entity.ChatMessage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * This Database repository implementation class, all business logic that related to database will be implemented here
 *
 * @author mobarak
 */
class ChatRepositoryImpl(private val db: AppDatabase) : ChatRepository {
    override fun observeChatMessages() = db.chatMessageDao().observeChatMessage()

    override fun observeChatMessageById(messageId: Long) =
        db.chatMessageDao().observeChatMessageById(messageId)

    override suspend fun getChatMessages() = db.chatMessageDao().getChatMessages()

    override suspend fun getChatMessageById(messageId: Long) =
        db.chatMessageDao().getChatMessageById(messageId)

    override suspend fun insertChatMessage(message: ChatMessage) {
        coroutineScope {
            launch {
                db.chatMessageDao().insertChatMessage(message)
            }
        }
    }

    override suspend fun updateChatMessage(message: ChatMessage) =
        db.chatMessageDao().updateChatMessage(message)

    override suspend fun deleteChatMessageById(messageId: Long) =
        db.chatMessageDao().deleteChatMessageById(messageId)

    override suspend fun deleteChatMessages() {
        db.chatMessageDao().deleteChatMessages()
    }
}