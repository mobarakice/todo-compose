package com.example.todocompose.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This is entity class
 * @author mobarak
 */
@Entity(tableName = "tasks")
data class Task @JvmOverloads constructor(
        @ColumnInfo(name = "title") var title: String = "",
        @ColumnInfo(name = "description") var description: String = "",
        @ColumnInfo(name = "is_completed") var isCompleted: Boolean = false,

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id") var id: Long? = null
) {

    val titleForList: String
        get() = title.ifEmpty { description }

    val isActive
        get() = !isCompleted
}