package com.example.todocompose

import android.app.Application
import androidx.room.Room
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.AppRepositoryImpl
import com.example.todocompose.data.db.AppDatabase

class TodoApplication : Application() {

    lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        repository = AppRepositoryImpl(
            Room.databaseBuilder(this, AppDatabase::class.java, "todo.db")
                .fallbackToDestructiveMigration()
                .build()
        )
    }
}