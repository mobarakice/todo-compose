package com.example.todocompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todocompose.data.AppRepository
import com.example.todocompose.ui.statistics.StatisticsScreen
import com.example.todocompose.ui.tasks.TaskScreen


@Composable
fun TodoNavGraph(
    repository: AppRepository,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    openDrawer: () -> Unit = {},
    startDestination: String = TodoDestinations.TASKS,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(TodoDestinations.TASKS) {
//            val taskViewModel: TasksViewModel = viewModel(
//                factory = TasksViewModel.provideFactory(
//                    repository = repository,
//                    owner = LocalSavedStateRegistryOwner.current
//                )
//            )

            TaskScreen(openDrawer)
//            HomeRoute(
//                homeViewModel = homeViewModel,
//                isExpandedScreen = isExpandedScreen,
//                openDrawer = openDrawer,
//            )
        }
        composable(TodoDestinations.STATISTICS) {
//            val stastsViewModel: StatisticsViewModel = viewModel(
//                factory = StatisticsViewModel.provideFactory(repository,
//                owner = LocalSavedStateRegistryOwner.current)
//            )
//            InterestsRoute(
//                interestsViewModel = interestsViewModel,
//                isExpandedScreen = isExpandedScreen,
//                openDrawer = openDrawer
//            )
            StatisticsScreen(openDrawer)
        }
    }
}
