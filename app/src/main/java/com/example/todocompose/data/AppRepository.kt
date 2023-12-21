package com.example.todocompose.data

import com.example.todocompose.data.db.ChatRepository
import com.example.todocompose.data.db.TaskRepository

/**
 * This is app repository class and handle all kind data(local,preference, remote) related operation through this
 *
 * @author mobarak
 */
interface AppRepository {
    fun getTaskRepository(): TaskRepository
    fun getChatRepository(): ChatRepository
}