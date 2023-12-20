package com.example.todocompose.ui.gemini

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todocompose.R
import com.example.todocompose.ui.theme.Typography


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
fun MessageScreen(
    openDrawer: () -> Unit,
    viewModel: MessageViewModel? = null,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    Scaffold(
        topBar = {
            MessageTopAppBar(openDrawer)
        }
    ) {
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
                val msg = getDummyData()
                MessageItem(messages = msg)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(56.dp)
                    .fillMaxWidth() // Color for child b
            ) {
                MessageInputBox(message = "")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun onChange(string: String) {

}

@Preview
@Composable
fun PreScreen() {
    MessageScreen(openDrawer = { /*TODO*/ }, viewModel = null)
}

@Preview
@Composable
fun PreTopBar() {
    MessageTopAppBar { }
}

@Preview
@Composable
fun PreMB() {
    MessageInputBox(message = "Ask your question here")
}

@Composable
fun MessageInputBox(
    modifier: Modifier = Modifier.fillMaxSize(),
    message: String,
    onMessageChanged: (String) -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
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
                .clickable {}
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                unfocusedTextColor = Color.LightGray,
                focusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            )
            var text by remember { mutableStateOf(message) }
            Icon(painterResource(id = R.drawable.tag_faces_black_24dp_1), null, tint = Color.White)
            TextField(
                value = text,
                onValueChange = {
                    text = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.title_hint),
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
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable {

                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(id = R.drawable.ic_microphone),
                null, tint = Color.White
            )
        }
    }
}

@Composable
fun MessageItem(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            if(message.id == 1){
                MessageReceiveItem(item = message)
            }else{
                MessageSendItem(item = message)
            }
        }
    }
}

@Preview
@Composable
fun PreviewMessageItem() {
    val msg = getDummyData()
    MessageItem(messages = msg)
}

fun getDummyData() = listOf<Message>(
    Message("Hello",5),
    Message("Hello, how can I help you",1)
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
        Text(text = item.message)
        Spacer(modifier = Modifier.size(8.dp))
        Icon(Icons.Filled.AccountCircle, contentDescription = null)
    }
}

@Preview
@Composable
fun PreviewMessageSendItem() {
    MessageSendItem(item = Message("Hello", 1))
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
        Text(text = item.message)
    }
}

@Preview
@Composable
fun PreviewMessageReceiveItem() {
    MessageReceiveItem(item = Message("Hello, how can I help you?", 2))
}

data class Message(val message: String, val id: Int)