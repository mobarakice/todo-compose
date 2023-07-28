package com.example.todocompose.ui.statistics


import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.R
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.TaskRepository
import com.example.todocompose.data.db.entity.Task
import com.example.todocompose.utils.Result
import com.example.todocompose.utils.WhileUiSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the statistics screen.
 */
data class StatisticsUiState(
    val isEmpty: Boolean = false,
    val isLoading: Boolean = false,
    val activeTasksPercent: Float = 0f,
    val completedTasksPercent: Float = 0f
)

/**
 * ViewModel for the statistics screen.
 */

class StatisticsViewModel(
    taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> =
        taskRepository.observeTasks()
            .map { Result.Success(it) }
            .catch<Result<List<Task>>> { emit(Result.Error(R.string.loading_tasks_error)) }
            .map { taskAsync -> produceStatisticsUiState(taskAsync) }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = StatisticsUiState(isLoading = true)
            )

    fun refresh() {
        viewModelScope.launch {
            //taskRepository.refresh()
        }
    }

    private fun produceStatisticsUiState(taskLoad: Result<List<Task>>) =
        when (taskLoad) {
            Result.Loading -> {
                StatisticsUiState(isLoading = true, isEmpty = true)
            }
            is Result.Error -> {
                StatisticsUiState(isEmpty = true, isLoading = false)
            }
            is Result.Success -> {
                val stats = getActiveAndCompletedStats(taskLoad.data)
                StatisticsUiState(
                    isEmpty = taskLoad.data.isEmpty(),
                    activeTasksPercent = stats.activeTasksPercent,
                    completedTasksPercent = stats.completedTasksPercent,
                    isLoading = false
                )
            }
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
                    return StatisticsViewModel(repository.getTaskRepository()) as T
                }
            }
    }
}