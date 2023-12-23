package com.example.todocompose.utils

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.todocompose.R

/**
 * Display an initial empty state or swipe to refresh content.
 *
 * @param loading (state) when true, display a loading spinner over [content]
 * @param empty (state) when true, display [emptyContent]
 * @param emptyContent (slot) the content to display for the empty state
 * @param onRefresh (event) event to request refresh
 * @param modifier the modifier to apply to this layout.
 * @param content (slot) the main content to show
 */
@Composable
@ExperimentalComposeApi
fun LoadingContent(
    loading: Boolean,
    empty: Boolean,
    emptyContent: @Composable () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (empty) {
        emptyContent()
    } else {
        content()
    }
}

@Composable
fun CustomSnackbar(snackbarState: SnackbarHostState) =
    SnackbarHost(hostState = snackbarState) { data ->
        // custom snackbar with the custom border
        Snackbar(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.error,
            shape = CircleShape,
            snackbarData = data
        )
    }

@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String?,
    dialogText: String,
    icon: Int,
) {
    AlertDialog(
//        icon = {
////            Icon(painter = painterResource(id = icon), contentDescription = "Example Icon")
//        },
        title = {
            Text(text = dialogTitle?:"")
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
//        tonalElevation = 8.dp

    )
}

@Preview
@Composable
fun PreviewAlertDialog(){
    CustomAlertDialog(
        onDismissRequest = { },
        onConfirmation = { },
        dialogTitle = stringResource(id = R.string.microphone_permission),
        dialogText = stringResource(id = R.string.microphone_permission_explanation),
        icon = R.drawable.ic_mic
    )
}

