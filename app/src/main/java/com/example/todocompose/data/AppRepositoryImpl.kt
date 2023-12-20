package com.example.todocompose.data

import com.example.todocompose.data.db.AppDatabase
import com.example.todocompose.data.db.TaskRepository
import com.example.todocompose.data.db.TaskRepositoryImpl

/**
 * This app repository implementation class and used to access all kind of data like local, preference or remote(network)
 *
 * @author mobarak
 */
class AppRepositoryImpl(private val db: AppDatabase) : AppRepository {
    override fun getTaskRepository() = TaskRepositoryImpl(db = db)
}