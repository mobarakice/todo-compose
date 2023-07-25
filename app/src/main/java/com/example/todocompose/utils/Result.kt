package com.example.todocompose.utils

/**
 * A generic class that holds a loading signal or the result of an async operation.
 */
sealed class Result<out T> {
    object Loading : Result<Nothing>()

    data class Error(val errorMessage: Int) : Result<Nothing>()

    data class Success<out T>(val data: T) : Result<T>()
}