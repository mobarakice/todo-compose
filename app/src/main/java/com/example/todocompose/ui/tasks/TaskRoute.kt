package com.example.todocompose.ui.tasks

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todocompose.R

@Composable
fun TaskRoute(
    homeViewModel: TasksViewModel,
    openDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // UiState of the HomeScreen
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    TaskRoute(
        viewModel = homeViewModel,
        onRefreshTasks = { homeViewModel.refresh() },
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
    )
}


@Composable
fun TaskRoute(
    viewModel: TasksViewModel,
    onRefreshTasks: () -> Unit,
    openDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // Construct the lazy list states for the list and the details outside of deciding which one to
    // show. This allows the associated state to survive beyond that decision, and therefore
    // we get to preserve the scroll throughout any changes to the content.
    val taskListLazyListState = rememberLazyListState()
    TaskScreen(
            userMessage = R.string.empty_task_message,
            onUserMessageDisplayed = { },
            onAddTask = {  },
            onTaskClick = {  },
            openDrawer = { openDrawer() },
        viewModel = viewModel
    )
}