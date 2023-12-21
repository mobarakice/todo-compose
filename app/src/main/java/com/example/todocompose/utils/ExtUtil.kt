package com.example.todocompose.utils

import com.example.todocompose.data.db.entity.ChatMessage
import com.example.todocompose.ui.gemini.Message

//object ExtUtil {
fun Message.toChatMessage() = ChatMessage(message = this.message, messageType = this.type)
fun ChatMessage.toMessage() =
    this.messageType?.let { Message(message = this.message, type = it) }
//}

fun List<ChatMessage>.toMessages(): List<Message> = this.mapNotNull { chatMessage ->
    chatMessage.toMessage()
}