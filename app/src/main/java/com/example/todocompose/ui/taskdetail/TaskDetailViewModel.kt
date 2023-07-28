package com.example.todocompose.ui.taskdetail

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.R
import com.example.todocompose.TodoDestinationsArgs
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.TaskRepository
import com.example.todocompose.data.db.entity.Task
import com.example.todocompose.utils.Result
import com.example.todocompose.utils.WhileUiSubscribed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the Details screen.
 */
data class TaskDetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isTaskDeleted: Boolean = false
)

/**
 * ViewModel for the Details screen.
 */
class TaskDetailViewModel(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val taskId: Long = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]!!

    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isTaskDeleted = MutableStateFlow(false)
    private val _taskAsync = taskRepository.observeTaskById(taskId)
        .map { handleTask(it) }
        .catch { emit(Result.Error(R.string.loading_task_error)) }

    val uiState: StateFlow<TaskDetailUiState> = combine(
        _userMessage, _isLoading, _isTaskDeleted, _taskAsync
    ) { userMessage, isLoading, isTaskDeleted, taskAsync ->
        when (taskAsync) {
            Result.Loading -> {
                TaskDetailUiState(isLoading = true)
            }

            is Result.Error -> {
                TaskDetailUiState(
                    userMessage = taskAsync.errorMessage,
                    isTaskDeleted = isTaskDeleted
                )
            }

            is Result.Success -> {
                TaskDetailUiState(
                    task = taskAsync.data,
                    isLoading = isLoading,
                    userMessage = userMessage,
                    isTaskDeleted = isTaskDeleted
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = TaskDetailUiState(isLoading = true)
        )

    fun deleteTask() = viewModelScope.launch {
        taskRepository.deleteTaskById(taskId)
        _isTaskDeleted.value = true
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val task = uiState.value.task ?: return@launch
        if (completed) {
            taskRepository.updateCompleted(task.id, task.isCompleted)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            taskRepository.activateTask(task.id)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            //taskRepository.refreshTask(taskId)
            _isLoading.value = false
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    private fun handleTask(task: Task?): Result<Task?> {
        if (task == null) {
            return Result.Error(R.string.task_not_found)
        }
        return Result.Success(task)
    }

    companion object {
        fun provideFactory(
            repository: AppRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    //handle = defaultArgs
                    return TaskDetailViewModel(repository.getTaskRepository(),handle) as T
                }
            }
    }
}