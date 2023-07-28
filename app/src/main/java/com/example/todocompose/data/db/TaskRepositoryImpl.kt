package com.example.todocompose.data.db

import com.example.todocompose.data.db.entity.Task
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * This Database repository implementation class, all business logic that related to database will be implemented here
 *
 * @author mobarak
 */
class TaskRepositoryImpl(private val db: AppDatabase) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> {
        return db.taskDao().observeTasks()
//        return flow {
//            val dummy = mutableListOf<Task>()
//            for (i in 0..5){
//                dummy.add(Task("Task $i","Description $i", id = Random.nextLong()))
//            }
//            delay(100)
//            emit(dummy)
//        }
    }


    override fun observeTaskById(taskId: Long): Flow<Task> {
        return db.taskDao().observeTaskById(taskId)
    }

    override suspend fun getTasks(): List<Task> {
        return db.taskDao().getTasks()
    }

    override suspend fun getTaskById(taskId: Long): Task? = db.taskDao().getTaskById(taskId)

    override suspend fun insertTask(task: Task) {
        coroutineScope {
            launch { db.taskDao().insertTask(task) }
        }
    }

    override suspend fun updateTask(task: Task): Int {
        return db.taskDao().updateTask(task)
    }

    override suspend fun updateCompleted(taskId: Long?, completed: Boolean): Int {
        taskId?.let {
            return db.taskDao().updateCompleted(taskId, completed)
        }
        return -1
    }

    override suspend fun deleteTaskById(taskId: Long): Int {
        return db.taskDao().deleteTaskById(taskId)
    }

    override suspend fun deleteTasks() {
        db.taskDao().deleteTasks()
    }

    override suspend fun deleteCompletedTasks(): Int {
        return db.taskDao().deleteCompletedTasks()
    }

    override suspend fun activateTask(taskId: Long?) {
        taskId?.let {
            updateCompleted(taskId = taskId, completed = false)
        }
    }
}