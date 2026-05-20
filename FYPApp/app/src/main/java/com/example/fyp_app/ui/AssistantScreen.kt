package com.example.fyp_app.ui.assistant

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fyp_app.R
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.viewmodels.ChatMessage
import com.example.fyp_app.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    userName: String,
    viewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    val greeting = stringResource(R.string.hello_greeting, userName)
    val systemInstruction = stringResource(R.string.system_instruction)
    val currentLanguage = context.resources.configuration.locales[0].toLanguageTag()

    LaunchedEffect(currentLanguage) {
        viewModel.initialize(currentLanguage, greeting, systemInstruction)
    }

    LaunchedEffect(viewModel.chatHistory.size) {
        if (viewModel.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatHistory.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Background Decorations (Placeholders for "green ground with leafs")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, AgroGreen.copy(alpha = 0.05f), AgroGreen.copy(alpha = 0.1f))
                    )
                )
        )
        
        // Custom background images
        Image(
            painter = painterResource(id = R.drawable.ground_leaves),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )
        Image(
            painter = painterResource(id = R.drawable.top_leaves),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopEnd).size(180.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.8f
        )

        Scaffold(
            topBar = {
                CustomAssistantHeader(onNavigateBack)
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.chatHistory) { message ->
                        ProfessionalChatBubble(message = message)
                    }
                }

                ProfessionalInputBar(
                    inputText = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CustomAssistantHeader(onNavigateBack: () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Robot Avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9))
                    .border(1.dp, Color(0xFFC8E6C9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 28.sp) 
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.agrobot_name),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.smart_assistant),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProfessionalChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Robot Avatar for bot messages
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9))
                    .border(1.dp, Color(0xFFC8E6C9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = if (isUser) 24.dp else 4.dp,
                topEnd = 24.dp,
                bottomStart = 24.dp,
                bottomEnd = if (isUser) 4.dp else 24.dp
            ),
            color = if (isUser) AgroGreen else Color.White,
            border = if (!isUser) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (!isUser) {
                    val helloText = stringResource(R.string.hello_emoji)
                    val hasHello = message.text.contains("Hello", ignoreCase = true)
                    Text(
                        text = if (hasHello) helloText else stringResource(R.string.agrobot_name),
                        color = AgroGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                // Clean up the text: remove the "Hello!" part if we are showing it in the header
                val displayBody = if (!isUser && message.text.contains("Hello", ignoreCase = true)) {
                    message.text.substringAfter("!").trim()
                } else {
                    message.text
                }

                Text(
                    text = displayBody,
                    color = if (isUser) Color.White else Color(0xFF424242),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message.timestamp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun ProfessionalInputBar(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                placeholder = { Text(stringResource(R.string.ask_agrobot_placeholder), color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_leaf),
                        contentDescription = null,
                        tint = AgroGreen,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    VoiceInputButton(onResult = onValueChange)
                },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = AgroGreen,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                maxLines = 3,
                singleLine = false
            )

            Spacer(modifier = Modifier.width(12.dp))

            FloatingActionButton(
                onClick = onSend,
                containerColor = AgroGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceInputButton(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (spokenText != null) {
                onResult(spokenText)
            }
        }
    }
    
    val language = context.resources.configuration.locales[0].toLanguageTag()
    val prompt = stringResource(R.string.voice_prompt)

    IconButton(onClick = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
        speechRecognizerLauncher.launch(intent)
    }) {
        Icon(Icons.Filled.Mic, contentDescription = "Speak", tint = Color.Gray)
    }
}
