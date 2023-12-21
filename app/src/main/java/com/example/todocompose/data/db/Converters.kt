package com.example.todocompose.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromMessageType(value: MessageType) = value.name

    @TypeConverter
    fun toMessageType(value: String) =
        MessageType.entries.find { messageType -> messageType.name.equals(value) }
}