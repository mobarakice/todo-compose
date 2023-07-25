package com.example.todocompose.ui.tasks

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.R
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.entity.Task
import com.example.todocompose.utils.Event
import com.mobarak.todo.ui.tasks.FilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

sealed interface TaskUiState {
    val isLoading: Boolean
    val errorMessage: Int

    data class NoTasks(
        override val isLoading: Boolean = false,
        override val errorMessage: Int = R.string.no_tasks_all,
    ) : TaskUiState

    data class Tasks(
        val tasks: List<Task> = emptyList(),
        override val isLoading: Boolean = false,
        override val errorMessage: Int = -1,
    ) : TaskUiState
}

private data class TaskViewModelState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessages: Int,
) {

    fun toUiState(): TaskUiState =
        if (tasks.isEmpty()) {
            TaskUiState.NoTasks(
                isLoading = isLoading,
                errorMessage = errorMessages,
            )
        } else {
            TaskUiState.Tasks(
                tasks = tasks,
                isLoading = isLoading,
                errorMessage = errorMessages,
            )
        }
}

class TasksViewModel(
    private val repository: AppRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val viewModelState = MutableStateFlow(
        TaskViewModelState(
            isLoading = true,
            errorMessages = R.string.loading_tasks_error
        )
    )

    val uiState: StateFlow<TaskUiState> = viewModelState
        .map(TaskViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _currentFilteringLabel = MutableStateFlow(-1)
    val currentFilteringLabel: StateFlow<Int> = _currentFilteringLabel

    private val _noTasksLabel = MutableStateFlow(-1)
    val noTasksLabel: StateFlow<Int> = _noTasksLabel

    private val _noTaskIconRes = MutableStateFlow(-1)
    val noTaskIconRes: StateFlow<Int> = _noTaskIconRes

    private val _tasksAddViewVisible = MutableStateFlow(false)
    val tasksAddViewVisible: StateFlow<Boolean> = _tasksAddViewVisible

    private val _snackbarText = MutableStateFlow(Event(-1))
    val snackbarText: StateFlow<Event<Int>> = _snackbarText

    private val _openTaskEvent = MutableStateFlow<Event<Long>>(Event(-1))
    val openTaskEvent: StateFlow<Event<Long>> = _openTaskEvent

    private val _newTaskEvent = MutableStateFlow(Event(Unit))
    val newTaskEvent: StateFlow<Event<Unit>> = _newTaskEvent

    private var resultMessageShown: Boolean = false

//    // This LiveData depends on another so we can use a transformation.
//    val empty: LiveData<Boolean> = Transformations.map(_items) {
//        it.isEmpty()
//    }

    init {
        // Set initial state
        setFiltering(getSavedFilterType())
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
        savedStateHandle.set(TASKS_FILTER_SAVED_STATE_KEY, requestType)

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            FilterType.ALL_TASKS -> {
                setFilter(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.ic_no_task, true
                )
            }

            FilterType.ACTIVE_TASKS -> {
                setFilter(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp, false
                )
            }

            FilterType.COMPLETED_TASKS -> {
                setFilter(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp, false
                )
            }
        }
        // Refresh list
        loadTasks(false)
    }

    private fun setFilter(
        @StringRes filteringLabelString: Int,
        @StringRes noTasksLabelString: Int,
        @DrawableRes noTaskIconDrawable: Int,
        tasksAddVisible: Boolean
    ) {
        _currentFilteringLabel.value = filteringLabelString
        _noTasksLabel.value = noTasksLabelString
        _noTaskIconRes.value = noTaskIconDrawable
        _tasksAddViewVisible.value = tasksAddVisible
    }

    /**
     * @param forceUpdate Pass in true to refresh the data in the [AppRepository]
     */
    fun loadTasks(forceUpdate: Boolean) {

        viewModelState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            // Trigger repository requests in parallel
            val items = if (forceUpdate) {
                repository.getTaskRepository()
                    .observeTasks()
                    .distinctUntilChanged()
                    .stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(3000),
                        emptyList()
                    )
            } else {
                tasks
            }
            _tasks.update {
                items.value
            }


            val filterItems = filterItems(tasks.value, getSavedFilterType())

            viewModelState.update {
                it.copy(
                    tasks = filterItems,
                    isLoading = false,
                    currentFilteringLabel.value
                )
            }
        }
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

    fun refreshAll() {
        loadTasks(false)
    }

    private fun getSavedFilterType(): FilterType {
        return savedStateHandle.get(TASKS_FILTER_SAVED_STATE_KEY) ?: FilterType.ALL_TASKS
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

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewTask() {
        _newTaskEvent.value = Event(Unit)
    }

    /**
     * Called by Data Binding.
     */
    fun openTask(taskId: Long) {
        _openTaskEvent.value = Event(taskId)
    }

    fun showEditResultMessage(result: Int) {
        if (resultMessageShown) return
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
        }
        resultMessageShown = true
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

}

// Used to save the current filtering in SavedStateHandle.
const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"
const val EDIT_RESULT_OK = 101
const val ADD_EDIT_RESULT_OK = 102
const val DELETE_RESULT_OK = 103
