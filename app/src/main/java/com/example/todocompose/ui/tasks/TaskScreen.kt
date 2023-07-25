package com.example.todocompose.ui.tasks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todocompose.R
import com.example.todocompose.data.db.entity.Task
import com.example.todocompose.ui.theme.TodoComposeTheme
import com.example.todocompose.ui.theme.Typography
import com.example.todocompose.utils.LoadingContent
import com.mobarak.todo.ui.tasks.FilterType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    @StringRes userMessage: Int,
    onAddTask: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onUserMessageDisplayed: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        topBar = {
                 TasksTopAppBar(
                     openDrawer = { openDrawer() },
                     onFilterAllTasks = { viewModel.setFiltering(FilterType.ALL_TASKS) },
                     onFilterActiveTasks = { viewModel.setFiltering(FilterType.ACTIVE_TASKS) },
                     onFilterCompletedTasks = { viewModel.setFiltering(FilterType.COMPLETED_TASKS) },
                     onClearCompletedTasks = { viewModel.clearCompletedTasks() },
                     onRefresh = {viewModel.refresh()},
                     modifier = Modifier.fillMaxWidth()
                 )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.add_task))
            }
        },
        content = { innerPadding ->
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TasksContent(
                loading = uiState.isLoading,
                tasks = uiState.items,
                currentFilteringLabel = uiState.filteringUiInfo.currentFilteringLabel,
                noTasksLabel = uiState.filteringUiInfo.noTasksLabel,
                noTasksIconRes = uiState.filteringUiInfo.noTaskIconRes,
                onRefresh = viewModel::refresh,
                onTaskClick = onTaskClick,
                onTaskCheckedChange = viewModel::completeTask,
                modifier = Modifier.padding(innerPadding)
            )

            // Check for user messages to display on the screen
            uiState.userMessage?.let { message ->
                val snackbarText = stringResource(message)
                LaunchedEffect(snackbarHostState,viewModel, message, snackbarText) {
                    snackbarHostState.showSnackbar(snackbarText)
                    viewModel.snackbarMessageShown()
                }
            }

            // Check if there's a userMessage to show to the user
            val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
            LaunchedEffect(userMessage) {
                if (userMessage != 0) {
                    viewModel.showEditResultMessage(userMessage)
                    currentOnUserMessageDisplayed()
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeApi::class)
@Composable
private fun TasksContent(
    loading: Boolean,
    tasks: List<Task>,
    @StringRes currentFilteringLabel: Int,
    @StringRes noTasksLabel: Int,
    @DrawableRes noTasksIconRes: Int,
    onRefresh: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LoadingContent(
        loading = loading,
        empty = tasks.isEmpty() && !loading,
        emptyContent = { TasksEmptyContent(noTasksLabel, noTasksIconRes, modifier) },
        onRefresh = onRefresh
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
        ) {
            Text(
                text = stringResource(currentFilteringLabel),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.list_item_padding),
                    vertical = dimensionResource(id = R.dimen.vertical_margin)
                ),
                style = Typography.headlineSmall
            )
            LazyColumn {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = onTaskClick,
                        onCheckedChange = { onTaskCheckedChange(task, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical = dimensionResource(id = R.dimen.list_item_padding),
            )
            .clickable { onTaskClick(task) }
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = task.titleForList,
            style = Typography.titleMedium,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.horizontal_margin)
            ),
            textDecoration = if (task.isCompleted) {
                TextDecoration.LineThrough
            } else {
                null
            }
        )
    }
}

@Composable
private fun TasksEmptyContent(
    @StringRes noTasksLabel: Int,
    @DrawableRes noTasksIconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = noTasksIconRes),
            contentDescription = stringResource(R.string.no_tasks_image_content_description),
            modifier = Modifier.size(96.dp)
        )
        Text(stringResource(id = noTasksLabel))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopAppBar(
    openDrawer: () -> Unit,
    onFilterAllTasks: () -> Unit,
    onFilterActiveTasks: () -> Unit,
    onFilterCompletedTasks: () -> Unit,
    onClearCompletedTasks: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.task_list),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            FilterTasksMenu(onFilterAllTasks, onFilterActiveTasks, onFilterCompletedTasks)
            MoreTasksMenu(onClearCompletedTasks, onRefresh)
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
        modifier = modifier
    )
}

@Composable
private fun FilterTasksMenu(
    onFilterAllTasks: () -> Unit,
    onFilterActiveTasks: () -> Unit,
    onFilterCompletedTasks: () -> Unit
) {
    TopAppBarDropdownMenu(
        iconContent = {
            Icon(
                painterResource(id = R.drawable.ic_filter_list),
                stringResource(id = R.string.menu_filter)
            )
        }
    ) { closeMenu ->
        DropdownMenuItem(onClick = { onFilterAllTasks(); closeMenu() }, text = {
            Text(text = stringResource(id = R.string.nav_all))
        })
        DropdownMenuItem(onClick = { onFilterActiveTasks(); closeMenu() }, text = {
            Text(text = stringResource(id = R.string.nav_active))
        })
        DropdownMenuItem(onClick = { onFilterCompletedTasks(); closeMenu() }, text = {
            Text(text = stringResource(id = R.string.nav_completed))
        })
    }
}

@Composable
private fun MoreTasksMenu(
    onClearCompletedTasks: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBarDropdownMenu(
        iconContent = {
            Icon(Icons.Filled.MoreVert, stringResource(id = R.string.menu_filter))
        }
    ) { closeMenu ->
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_clear)) },
            onClick = { onClearCompletedTasks(); closeMenu() })

        DropdownMenuItem(onClick = { onRefresh(); closeMenu() }, text = {
            Text(text = stringResource(id = R.string.refresh))
        })
    }
}

@Composable
private fun TopAppBarDropdownMenu(
    iconContent: @Composable () -> Unit,
    content: @Composable ColumnScope.(() -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded = !expanded }) {
            iconContent()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            content { expanded = !expanded }
        }
    }
}

@Composable
fun OpenTaskFilterPopup() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_filter_list),
                contentDescription = "Localized description"
            )
        }
        DropdownMenu(
            expanded = expanded,
            properties = PopupProperties(focusable = true),
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { /* Handle edit! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null
                    )
                })
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = { /* Handle settings! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null
                    )
                })
            Divider()
            DropdownMenuItem(
                text = { Text("Send Feedback") },
                onClick = { /* Handle send feedback! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null
                    )
                },
                trailingIcon = { Text("F11", textAlign = TextAlign.Center) })
        }
    }
}


@Composable
@Preview
fun PreviewTaskScreen() {
    TodoComposeTheme {
        //TaskScreen(openDrawer = {})
    }
}