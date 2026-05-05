package com.chamaflow.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.ChatMessage
import com.chamaflow.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

private val WhatsAppOutgoing = Color(0xFFE7FFDB)
private val WhatsAppIncoming = Color(0xFFFFFFFF)
private val WhatsAppBackground = Color(0xFFE5DDD5)
private val WhatsAppTeal = Color(0xFF075E54)
private val WhatsAppCheckBlue = Color(0xFF34B7F1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chamaId: String,
    userId: String,
    userName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chamaId) {
        viewModel.loadMessages(chamaId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Groups, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Chama Group", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("online", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Videocam, null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Call, null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WhatsAppTeal)
            )
        },
        bottomBar = {
            WhatsAppChatInput(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = {
                    viewModel.sendMessage(chamaId, userId, userName, messageText)
                    messageText = ""
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(WhatsAppBackground)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = WhatsAppTeal)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val groupedMessages = uiState.messages.groupBy { formatHeaderDate(it.timestamp) }
                    
                    groupedMessages.forEach { (date, messages) ->
                        item {
                            WhatsAppDateHeader(date)
                        }
                        items(messages, key = { it.id }) { message ->
                            WhatsAppChatBubble(message, isMe = message.senderId == userId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppDateHeader(date: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(
            color = Color(0xFFD1E9F6),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 0.5.dp
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF505C5E)
            )
        }
    }
}

@Composable
fun WhatsAppChatBubble(message: ChatMessage, isMe: Boolean) {
    val bubbleColor = if (isMe) WhatsAppOutgoing else WhatsAppIncoming
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = if (isMe) 8.dp else 0.dp,
        bottomEnd = if (isMe) 0.dp else 8.dp
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(vertical = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                
                Box {
                    Column {
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp, end = 40.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimestamp(message.timestamp),
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        if (isMe) {
                            Spacer(Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = WhatsAppCheckBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppChatInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(25.dp),
            color = Color.White,
            modifier = Modifier.weight(1f),
            shadowElevation = 1.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.SentimentSatisfied, null, tint = Color.Gray)
                }
                TextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    placeholder = { Text("Message", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 6
                )
                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.AttachFile, null, tint = Color.Gray)
                }
                if (messageText.isBlank()) {
                    IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                    }
                }
            }
        }
        
        Spacer(Modifier.width(6.dp))
        
        FloatingActionButton(
            onClick = { if (messageText.isNotBlank()) onSend() },
            containerColor = WhatsAppTeal,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
            elevation = FloatingActionButtonDefaults.elevation(2.dp)
        ) {
            Icon(
                imageVector = if (messageText.isBlank()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatHeaderDate(timestamp: Long): String {
    val cal = Calendar.getInstance()
    val now = cal.clone() as Calendar
    cal.timeInMillis = timestamp
    
    return when {
        isSameDay(cal, now) -> "TODAY"
        isYesterday(cal, now) -> "YESTERDAY"
        else -> SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(timestamp)).uppercase()
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal: Calendar, now: Calendar): Boolean {
    val yesterday = now.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(cal, yesterday)
}
