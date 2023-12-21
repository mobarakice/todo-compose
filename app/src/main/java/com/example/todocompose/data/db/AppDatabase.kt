package com.example.todocompose.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todocompose.data.db.dao.ChatMessageDao
import com.example.todocompose.data.db.dao.TaskDao
import com.example.todocompose.data.db.entity.ChatMessage
import com.example.todocompose.data.db.entity.Task

/**
 * The Room database that contains the task table
 * @author mobarak
 */
@Database(entities = [Task::class, ChatMessage::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun chatMessageDao(): ChatMessageDao
}