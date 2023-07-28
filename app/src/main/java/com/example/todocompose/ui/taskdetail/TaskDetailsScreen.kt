@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todocompose.ui.taskdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todocompose.R
import com.example.todocompose.data.db.entity.Task
import com.example.todocompose.ui.theme.TodoComposeTheme
import com.example.todocompose.ui.theme.Typography
import com.example.todocompose.utils.LoadingContent

@Composable
fun TaskDetailScreen(
    onEditTask: (Long) -> Unit,
    onBack: () -> Unit,
    onDeleteTask: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TaskDetailTopAppBar(onBack = onBack, onDelete = viewModel::deleteTask)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditTask(viewModel.taskId) }) {
                Icon(Icons.Filled.Edit, stringResource(id = R.string.edit_task))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        EditTaskContent(
            loading = uiState.isLoading,
            empty = uiState.task == null && !uiState.isLoading,
            task = uiState.task,
            onRefresh = viewModel::refresh,
            onTaskCheck = viewModel::setCompleted,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if the task is deleted and call onDeleteTask
        LaunchedEffect(uiState.isTaskDeleted) {
            if (uiState.isTaskDeleted) {
                onDeleteTask()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailTopAppBar(onBack: () -> Unit, onDelete: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.task_details))
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
            }
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, stringResource(id = R.string.menu_delete_task))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalComposeApi::class)
@Composable
private fun EditTaskContent(
    loading: Boolean,
    empty: Boolean,
    task: Task?,
    onTaskCheck: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenPadding = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.horizontal_margin),
        vertical = dimensionResource(id = R.dimen.vertical_margin),
    )
    val commonModifier = modifier
        .fillMaxWidth()
        .then(screenPadding)

    LoadingContent(
        loading = loading,
        empty = empty,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = commonModifier
            )
        },
        onRefresh = onRefresh
    ) {
        Column(commonModifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(screenPadding),

                ) {
                if (task != null) {
                    Checkbox(task.isCompleted, onTaskCheck)
                    Column {
                        Text(text = task.title, style = Typography.headlineMedium)
                        Text(text = task.description, style = Typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditTaskContentPreview() {
    TodoComposeTheme {
        Surface {
            EditTaskContent(
                loading = false,
                empty = false,
                Task(
                    title = "Title",
                    description = "Description",
                    isCompleted = false,
                    id = 0
                ),
                onTaskCheck = { },
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
private fun EditTaskContentTaskCompletedPreview() {
    TodoComposeTheme {
        Surface {
            EditTaskContent(
                loading = false,
                empty = false,
                Task(
                    title = "Title",
                    description = "Description",
                    isCompleted = false,
                    id = 0
                ),
                onTaskCheck = { },
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
private fun EditTaskContentEmptyPreview() {
    TodoComposeTheme {
        Surface {
            EditTaskContent(
                loading = false,
                empty = true,
                Task(
                    title = "Title",
                    description = "Description",
                    isCompleted = false,
                    id = 0
                ),
                onTaskCheck = { },
                onRefresh = { }
            )
        }
    }
}