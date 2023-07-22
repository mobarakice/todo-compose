package com.example.todocompose.ui.statistics

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.todocompose.data.AppRepository
import com.example.todocompose.data.db.entity.Task
import kotlinx.coroutines.launch


class StatisticsViewModel(
    private val repository: AppRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val tasks: LiveData<List<Task>> =
        repository.getTaskRepository().observeTasks().asLiveData()
    private val _dataLoading = MutableLiveData<Boolean>(false)
    private val stats: LiveData<StatsResult?> = tasks.map {
        if (it.isNotEmpty()) {
            getActiveAndCompletedStats(it)
        } else {
            null
        }
    }

    val activeTasksPercent = stats.map {
        it?.activeTasksPercent ?: 0f
    }
    val completedTasksPercent: LiveData<Float> = stats.map { it?.completedTasksPercent ?: 0f }
    val dataLoading: LiveData<Boolean> = _dataLoading
    val error: LiveData<Boolean> = tasks.map { it.isEmpty() }
    val empty: LiveData<Boolean> = tasks.map { it.isEmpty() }

    fun refresh() {
        _dataLoading.value = true
        viewModelScope.launch {
            repository.getTaskRepository().getTasks()
            _dataLoading.value = false
        }
    }

    companion object {
        fun provideFactory(
            myRepository: AppRepository,
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
                    return StatisticsViewModel(myRepository, handle) as T
                }
            }
    }
}