package com.example.todocompose.ui.tasks

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TaskRoute(
    homeViewModel: TasksViewModel,
    openDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // UiState of the HomeScreen
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    TaskRoute(
        uiState = uiState,
        onRefreshTasks = { homeViewModel.refreshAll() },
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
    )
}


@Composable
fun TaskRoute(
    uiState: TaskUiState,
    onRefreshTasks: () -> Unit,
    openDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // Construct the lazy list states for the list and the details outside of deciding which one to
    // show. This allows the associated state to survive beyond that decision, and therefore
    // we get to preserve the scroll throughout any changes to the content.
    val taskListLazyListState = rememberLazyListState()
    val taskListStates = when (uiState) {
        is TaskUiState.Tasks -> uiState.tasks
        is TaskUiState.NoTasks -> emptyList()
    }.associate { task ->
        key(task.id) {
            task.id to rememberLazyListState()
        }
    }

    TaskScreen(
        uiState = uiState,
        onRefreshPosts = onRefreshTasks,
        openDrawer = openDrawer,
        taskListLazyListState = taskListLazyListState,
        snackbarHostState = snackbarHostState
    )
}