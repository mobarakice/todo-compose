package com.example.todocompose

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Destinations used in the [TodoApp].
 */
object TodoDestinations {
    const val TASKS = "tasks"
    const val STATISTICS = "statistics"
}

/**
 * Models the navigation actions in the app.
 */
class TodoNavigationActions(val navController: NavHostController) {
    val navigateToTask: () -> Unit = {
        navController.navigate(TodoDestinations.TASKS) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
    val navigateToStatistics: () -> Unit = {
        navController.navigate(TodoDestinations.STATISTICS) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
