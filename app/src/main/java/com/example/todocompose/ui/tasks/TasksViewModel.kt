package com.example.todocompose.ui.tasks

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.ADD_EDIT_RESULT_OK
import com.example.todocompose.DELETE_RESULT_OK
import com.example.todocompose.EDIT_RESULT_OK
import com.example.todocompose.R
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.entity.Task
import com.example.todocompose.utils.Result
import com.example.todocompose.utils.WhileUiSubscribed
import com.mobarak.todo.ui.tasks.FilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

data class TasksUiState(
    val items: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null
)

data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_tasks_all,
    val noTaskIconRes: Int = R.drawable.logo_no_fill,
)

class TasksViewModel(
    private val repository: AppRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _savedFilterType = savedStateHandle.getStateFlow(
        TASKS_FILTER_SAVED_STATE_KEY, FilterType.ALL_TASKS
    )

    private val _filterUiInfo =
        _savedFilterType.map { getFilteringUiInfo(it) }.distinctUntilChanged()

    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)

    private val _filteredTasksAsync =
        combine(repository.getTaskRepository().observeTasks(), _savedFilterType) { tasks, type ->
            filterItems(tasks, type)
        }.map { Result.Success(it) }
            .catch<Result<List<Task>>> { emit(Result.Error(R.string.loading_tasks_error)) }

    val uiState: StateFlow<TasksUiState> = combine(
        _filterUiInfo, _isLoading, _userMessage, _filteredTasksAsync
    ) { filterUiInfo, isLoading, userMessage, tasksAsync ->
        when (tasksAsync) {
            Result.Loading -> {
                TasksUiState(isLoading = true)
            }

            is Result.Error -> {
                TasksUiState(userMessage = tasksAsync.errorMessage)
            }

            is Result.Success -> {
                TasksUiState(
                    items = tasksAsync.data,
                    filteringUiInfo = filterUiInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = TasksUiState(isLoading = true)
        )

    init {
        // Set initial state
        loadTasks(true)
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be [FilterType.ALL_TASKS],
     * [FilterType.COMPLETED_TASKS], or
     * [FilterType.ACTIVE_TASKS]
     */
    fun setFiltering(requestType: FilterType) {
        savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = requestType
    }

    private fun getFilteringUiInfo(requestType: FilterType): FilteringUiInfo =
        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            FilterType.ALL_TASKS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.ic_no_task
                )
            }

            FilterType.ACTIVE_TASKS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp
                )
            }

            FilterType.COMPLETED_TASKS -> {
                FilteringUiInfo(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp
                )
            }
        }

    /**
     * @param forceUpdate Pass in true to refresh the data in the [AppRepository]
     */
    fun loadTasks(forceUpdate: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getTaskRepository().observeTasks()
            _isLoading.value = false
        }
//        viewModelScope.launch {
//            // Trigger repository requests in parallel
//            val items = if (forceUpdate) {
//                repository.getTaskRepository()
//                    .observeTasks()
//                    .distinctUntilChanged()
//                    .stateIn(
//                        viewModelScope,
//                        SharingStarted.WhileSubscribed(3000),
//                        emptyList()
//                    )
//            } else {
//                emptyList()
//            }
//
//            uiState.update {
//                it.copy(
//                    tasks = filterItems,
//                    isLoading = false,
//                    currentFilteringLabel.value
//                )
//            }
//        }
    }

    private fun filterItems(tasks: List<Task>, filteringType: FilterType): List<Task> {
        val tasksToShow = ArrayList<Task>()
        // We filter the tasks based on the requestType
        for (task in tasks) {
            when (filteringType) {
                FilterType.ALL_TASKS -> tasksToShow.add(task)
                FilterType.ACTIVE_TASKS -> if (task.isActive) {
                    tasksToShow.add(task)
                }

                FilterType.COMPLETED_TASKS -> if (task.isCompleted) {
                    tasksToShow.add(task)
                }
            }
        }
        return tasksToShow
    }

    fun refresh() {
        loadTasks(false)
    }

    companion object {
        private val TAG = TasksViewModel::class.java.simpleName
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
                    return TasksViewModel(repository, handle) as T
                }
            }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.getTaskRepository().deleteCompletedTasks()
            showSnackbarMessage(R.string.completed_tasks_cleared)
            refresh()
        }
    }

    fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
        repository.getTaskRepository().updateCompleted(task.id, completed)
        if (completed) {
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

}

// Used to save the current filtering in SavedStateHandle.
const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"
