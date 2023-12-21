package com.example.todocompose.data.db.dao

import androidx.room.*
import com.example.todocompose.data.db.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the ChatMessage table.
 * @author Mobarak
 */
@Dao
interface ChatMessageDao {
    /**
     * Observes list of ChatMessages.
     *
     * @return all ChatMessages.
     */
    @Query("SELECT * FROM CHAT_MESSAGE")
    fun observeChatMessage(): Flow<List<ChatMessage>>

    /**
     * Observes a single ChatMessage.
     *
     * @param id the ChatMessage id.
     * @return the ChatMessage with id.
     */
    @Query("SELECT * FROM chat_message WHERE id = :id")
    fun observeChatMessageById(id: Long): Flow<ChatMessage>

    /**
     * Select all ChatMessages from the ChatMessages table.
     *
     * @return all ChatMessages.
     */
    @Query("SELECT * FROM chat_message")
    suspend fun getChatMessages(): List<ChatMessage>

    /**
     * Select a ChatMessage by id.
     *
     * @param id the ChatMessage id.
     * @return the ChatMessage with id.
     */
    @Query("SELECT * FROM chat_message WHERE id = :id")
    suspend fun getChatMessageById(id: Long): ChatMessage?

    /**
     * Insert a ChatMessage in the database. If the ChatMessage already exists, replace it.
     *
     * @param ChatMessage the ChatMessage to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(chatMessage: ChatMessage)

    /**
     * Update a ChatMessage.
     *
     * @param ChatMessage ChatMessage to be updated
     * @return the number of ChatMessages updated. This should always be 1.
     */
    @Update
    suspend fun updateChatMessage(chatMessage: ChatMessage): Int


    /**
     * Delete a ChatMessage by id.
     *
     * @return the number of ChatMessages deleted. This should always be 1.
     */
    @Query("DELETE FROM chat_message WHERE id = :id")
    suspend fun deleteChatMessageById(id: Long): Int

    /**
     * Delete all ChatMessages.
     */
    @Query("DELETE FROM chat_message")
    suspend fun deleteChatMessages()

}