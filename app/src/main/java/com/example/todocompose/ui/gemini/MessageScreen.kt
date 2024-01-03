package com.example.todocompose.ui.gemini

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todocompose.R
import com.example.todocompose.data.db.MessageType
import com.example.todocompose.ui.theme.TodoComposeTheme
import com.example.todocompose.ui.theme.Typography
import com.example.todocompose.utils.CustomAlertDialog
import com.example.todocompose.utils.CustomSnackbar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MessageScreen(
    openDrawer: () -> Unit,
    viewModel: MessageViewModel,
    snackBarHostState: SnackbarHostState = SnackbarHostState()
) {
    val scope = rememberCoroutineScope()
    val snackBarState = remember { snackBarHostState }
    val context = LocalContext.current
    val micPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO) {
        if (it) {
            viewModel.sendNewMessage(context)
        } else {
            viewModel.updatePermissionDialogState(true)
        }
    }
    Scaffold(
        snackbarHost = {
            CustomSnackbar(snackbarState = snackBarState)
        },
        topBar = {
            MessageTopAppBar(openDrawer)
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ChatContent(viewModel, uiState, paddingValues, micPermissionState)

        //MicPermission(micPermissionState)

        // Check for user messages to display on the screen
        uiState.errorMessage?.let { errorMessage ->
            val snackBarText = stringResource(errorMessage)
            viewModel.updateErrorMessage(null)
            scope.launch {
                snackBarState.showSnackbar(snackBarText)
            }
        }

        // Check for user messages to display on the screen
        uiState.permissionDialog.let {
            if (it.first) {
                CustomAlertDialog(
                    onDismissRequest = {
                        viewModel.updatePermissionDialogState(false)
                    },
                    onConfirmation = {
                        micPermissionState.launchPermissionRequest()
                        viewModel.updatePermissionDialogState(false)
                    },
                    dialogTitle = stringResource(R.string.microphone_permission),
                    dialogText = it.second?.let { id ->
                        stringResource(id = id)
                    } ?: "",
                    icon = R.drawable.ic_mic
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatContent(
    viewModel: MessageViewModel,
    uiState: ChatUiState,
    paddingValues: PaddingValues,
    micPermissionState: PermissionState
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Take remaining space equally
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IndeterminateCircularIndicator(56.dp)
                }
            } else {
                val msg = uiState.messages
                MessageItem(messages = msg)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        when (val state = uiState.state) {
            is MessageState.MessageTypeAudio ->
                SpeakingAndListeningBox(
                    viewModel,
                    state
                )

            is MessageState.MessageTypeText ->
                MessageInputBox(
                    uiState.userMessage,
                    state,
                    viewModel,
                    micPermissionState = micPermissionState
                )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTopAppBar(openDrawer: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
        ),
        title = {
            Text(
                text = stringResource(id = R.string.message_title), maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
            }
        },
        windowInsets = TopAppBarDefaults.windowInsets,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SpeakingAndListeningBox(
    viewModel: MessageViewModel,
    state: MessageState.MessageTypeAudio
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            when (state) {
                MessageState.MessageTypeAudio.Loading -> ThinkingLoader(
                    modifier = Modifier.padding(8.dp),
                    resInt = R.string.thinking_loader
                )

                MessageState.MessageTypeAudio.Listening -> AnimatedListening()
                MessageState.MessageTypeAudio.Speaking -> AnimatedSpeaker()
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            Icons.Filled.Close,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(36.dp)
                .clickable {
                    viewModel.stopListening()
                },
            tint = MaterialTheme.colorScheme.inversePrimary
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MessageInputBox(
    message: String,
    state: MessageState.MessageTypeText,
    viewModel: MessageViewModel,
    micPermissionState: PermissionState
) {
    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .defaultMinSize(minHeight = 56.dp)
            .fillMaxWidth()
    ) {

        when (state) {
            MessageState.MessageTypeText.Loading ->
                ThinkingLoader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                        .padding(horizontal = 16.dp),
                    resInt = R.string.thinking_loader
                )

            MessageState.MessageTypeText.Typing ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .wrapContentHeight()
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp)
                            .weight(1f)
                    ) {
                        val textFieldColors = TextFieldDefaults.colors(
                            unfocusedPlaceholderColor = Color.LightGray,
                            focusedPlaceholderColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White
                        )
                        Icon(
                            painterResource(id = R.drawable.tag_faces_black_24dp_1),
                            null,
                            tint = Color.White
                        )
                        TextField(
                            value = message,
                            onValueChange = viewModel::updateUserMessage,
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.ask_your_question_here),
                                    style = Typography.titleMedium
                                )
                            },
                            textStyle = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize(),
                            colors = textFieldColors
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .clickable {
                                if (micPermissionState.status.isGranted) {
                                    viewModel.sendNewMessage(context)
                                } else {
                                    if (micPermissionState.status.shouldShowRationale) {
                                        viewModel.updateDialogText(R.string.microphone_permission_explanation)
                                    }
                                    micPermissionState.launchPermissionRequest()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(
                                if (message.isEmpty()) R.drawable.ic_mic
                                else R.drawable.ic_send
                            ),
                            null,
                            tint = Color.White
                        )
                    }
                }
        }

    }
}

@Composable
fun MessageItem(messages: List<Message>) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LazyColumn(state = listState) {
        items(messages) { message ->
            if (MessageType.RECEIVE == message.type) {
                MessageReceiveItem(item = message)
            } else {
                MessageSendItem(item = message)
            }
        }
        scope.launch {
            listState.layoutInfo.let {
                val index = if (messages.isNotEmpty()) {
                    messages.size - 1
                } else {
                    0
                }
                listState.animateScrollToItem(index)
            }
        }
    }
}

fun getDummyData() = listOf(
    Message("Hello", MessageType.SEND),
    Message("Hello, how can I help you?", MessageType.RECEIVE)
)

@Composable
fun MessageSendItem(item: Message) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .padding(start = 56.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            CustomText(message = item.message, MaterialTheme.colorScheme.inverseOnSurface)
        }
        ProfileIcon(start = 8.dp, end = 16.dp)
    }
}

@Composable
fun MessageReceiveItem(item: Message) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        ProfileIcon(R.drawable.ic_robot_solid, start = 16.dp, end = 8.dp)
        Row(
            modifier = Modifier
                .padding(end = 56.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            CustomText(message = item.message, MaterialTheme.colorScheme.inversePrimary)
        }
    }
}

@Composable
fun ProfileIcon(@DrawableRes resId: Int? = null, start: Dp, end: Dp) {
    Row(
        modifier = Modifier.size(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.size(start))
        val painter = if (resId != null)
            painterResource(id = resId)
        else
            rememberVectorPainter(image = Icons.Filled.AccountCircle)
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(modifier = Modifier.size(end))
    }
}

@Composable
fun CustomText(message: String, color: Color = MaterialTheme.colorScheme.inverseOnSurface) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(
                color = color,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Text(text = message, modifier = Modifier.padding(8.dp))
    }
}

//@Preview
//@Composable
//fun MessageScreenPreview() {
//    MessageScreen(openDrawer = { /*TODO*/ }, viewModel = null)
//}

@Preview
@Composable
fun SpeakingAndListeningBoxPreview() {
//    SpeakingAndListeningBox(state = MessageState.MessageTypeAudio.Listening)
}

@Preview
@Composable
fun TopBarPreview() {
    MessageTopAppBar { }
}

@Preview
@Composable
fun MessageItemPreview() {
    val msg = getDummyData()
    MessageItem(messages = msg)
}

@Preview
@Composable
fun MessageReceiveItemPreview() {
    MessageReceiveItem(Message("Hello, how can I help you?", MessageType.RECEIVE))
}

@Preview
@Composable
fun MessageSendItemPreview() {
    MessageSendItem(Message("Hello", MessageType.SEND))
}

@Preview
@Composable
fun ProfileIconPreview() {
    TodoComposeTheme {
        ProfileIcon(start = 16.dp, end = 16.dp)
    }
}


