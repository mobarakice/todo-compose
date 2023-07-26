package com.example.todocompose

import android.app.Activity
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todocompose.TodoDestinationsArgs.TASK_ID_ARG
import com.example.todocompose.TodoDestinationsArgs.TITLE_ARG
import com.example.todocompose.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.todocompose.data.AppRepository
import com.example.todocompose.ui.addedittask.AddEditTaskScreen
import com.example.todocompose.ui.addedittask.AddEditTaskViewModel
import com.example.todocompose.ui.statistics.StatisticsScreen
import com.example.todocompose.ui.tasks.TaskRoute
import com.example.todocompose.ui.tasks.TaskScreen
import com.example.todocompose.ui.tasks.TasksViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun TodoNavGraph(
    repository: AppRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    openDrawer: () -> Unit,
    startDestination: String = TodoDestinations.TASKS_ROUTE,
    navActions: TodoNavigationActions = remember(navController) {
        TodoNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            TodoDestinations.TASKS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            val taskViewModel: TasksViewModel = viewModel(
                factory = TasksViewModel.provideFactory(
                    repository = repository,
                    owner = LocalSavedStateRegistryOwner.current
                )
            )
            TaskScreen(
                userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                onAddTask = { navActions.navigateToAddEditTask(R.string.add_task, null) },
                onTaskClick = { task -> navActions.navigateToTaskDetail(task.id) },
                openDrawer = openDrawer,
                viewModel = taskViewModel
            )
        }
        composable(TodoDestinations.STATISTICS_ROUTE) {
                StatisticsScreen(openDrawer = openDrawer)
        }
        composable(
            TodoDestinations.ADD_EDIT_TASK_ROUTE,
            arguments = listOf(
                navArgument(TITLE_ARG) { type = NavType.IntType },
                navArgument(TASK_ID_ARG) { type = NavType.StringType; nullable = true },
            )
        ) { entry ->
            val taskId = entry.arguments?.getString(TASK_ID_ARG)
            val addEditViewModel: AddEditTaskViewModel = viewModel(
                factory = AddEditTaskViewModel.provideFactory(
                    repository = repository,
                    owner = LocalSavedStateRegistryOwner.current
                )
            )
            AddEditTaskScreen(
                topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
                onTaskUpdate = {
                    navActions.navigateToTasks(
                        if (taskId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
                    )
                },
                onBack = { navController.popBackStack() },
                viewModel = addEditViewModel
            )
        }
        composable(TodoDestinations.TASK_DETAIL_ROUTE) {
//            TaskDetailScreen(
//                onEditTask = { taskId ->
//                    navActions.navigateToAddEditTask(R.string.edit_task, taskId)
//                },
//                onBack = { navController.popBackStack() },
//                onDeleteTask = { navActions.navigateToTasks(DELETE_RESULT_OK) }
//            )
        }
    }
}

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
