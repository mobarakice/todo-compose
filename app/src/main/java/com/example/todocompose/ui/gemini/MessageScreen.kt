package com.example.todocompose.ui.gemini

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Send
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todocompose.R
import com.example.todocompose.data.db.MessageType
import com.example.todocompose.ui.theme.Typography

@Composable
fun MessageScreen(
    openDrawer: () -> Unit,
    viewModel: MessageViewModel,
    snackBarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        topBar = {
            MessageTopAppBar(openDrawer)
        }
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space equally
            ) {
                val msg = uiState.messages
                MessageItem(messages = msg)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(56.dp)
                    .fillMaxWidth() // Color for child b
            ) {
                MessageInputBox(
                    uiState.userMessage,
                    viewModel::updateMessage,
                    viewModel::sendNewMessage
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
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
fun MessageInputBox(
    message: String,
    onMessageChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable {}
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(
                    horizontal = 16.dp,
                )
                .weight(1f)
                .clickable {}
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
            //var text by remember { mutableStateOf("") }
            Icon(painterResource(id = R.drawable.tag_faces_black_24dp_1), null, tint = Color.White)
            TextField(
                value = message,
                onValueChange = onMessageChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.ask_your_question_here),
                        style = Typography.titleMedium
                    )
                },
                textStyle = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                colors = textFieldColors
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable {
                    onSend()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Send,
                null, tint = Color.White
            )
        }
    }
}

@Composable
fun MessageItem(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            if (MessageType.RECEIVE.equals(message.type)) {
                MessageReceiveItem(item = message)
            } else {
                MessageSendItem(item = message)
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
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        CustomText(message = item.message)
        Spacer(modifier = Modifier.size(8.dp))
        Icon(Icons.Filled.AccountCircle, contentDescription = null)
    }
}

@Composable
fun MessageReceiveItem(item: Message) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.AccountCircle, contentDescription = null)
        Spacer(modifier = Modifier.size(8.dp))
        CustomText(message = item.message)
    }
}

@Composable
fun CustomText(message: String) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(
                color = Color.LightGray,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Text(text = message, modifier = Modifier.padding(8.dp))
    }
}

//@Preview
//@Composable
//fun PreScreen() {
//    MessageScreen(openDrawer = { /*TODO*/ }, viewModel = null)
//}

@Preview
@Composable
fun PreTopBar() {
    MessageTopAppBar { }
}

@Preview
@Composable
fun PreMB() {
    val m = stringResource(R.string.ask_your_question_here)
    MessageInputBox(message = m, {}, {})
}

@Preview
@Composable
fun PreviewMessageItem() {
    val msg = getDummyData()
    MessageItem(messages = msg)
}

@Preview
@Composable
fun PreviewMessageReceiveItem() {
    MessageReceiveItem(Message("Hello, how can I help you?", MessageType.RECEIVE))
}

@Preview
@Composable
fun PreviewMessageSendItem() {
    MessageSendItem(Message("Hello", MessageType.SEND))
}