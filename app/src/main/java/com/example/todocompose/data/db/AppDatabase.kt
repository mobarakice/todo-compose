package com.example.todocompose.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todocompose.data.db.dao.TaskDao
import com.example.todocompose.data.db.entity.Task

/**
 * The Room database that contains the task table
 * @author mobarak
 */
@Database(entities = [Task::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}