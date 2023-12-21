package com.example.todocompose.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todocompose.data.db.MessageType

@Entity(tableName = "chat_message")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Long? = null,

    @ColumnInfo(name = "message") var message: String,
    @ColumnInfo(name = "message_type") var messageType: MessageType? = null
)
