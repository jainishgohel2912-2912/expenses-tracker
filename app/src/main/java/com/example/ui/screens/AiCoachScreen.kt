package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FinanceCard
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currency by viewModel.currency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()

    var chatInput by remember { mutableStateOf("") }
    var voiceLogInput by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("CHAT") } // "CHAT" or "QUICK_LOG"

    val lazyListState = rememberLazyListState()

    val bgColors = if (isDarkMode) {
        listOf(Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }

    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val chatBubbleUserBg = Color(0xFF3B82F6)
    val chatBubbleAiBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val chatBubbleAiText = if (isDarkMode) Color.White else Color(0xFF0F172A)

    // Listen to voice log parsing results and show toast notifications
    LaunchedEffect(key1 = true) {
        viewModel.voiceLogResult.collectLatest { result ->
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }

    // Scroll chat list to bottom automatically when message count grows
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgColors))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "AI Personal Finance Coach",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
        Text(
            "Powered by Gemini for insights & quick voice logging",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Segmented Control Tabs (Chat with Assistant / Quick Voice Logger)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                .padding(4.dp)
        ) {
            listOf("CHAT" to "AI Spending Coach", "QUICK_LOG" to "Voice / Text Logger").forEach { tab ->
                val isSelected = activeTab == tab.first
                val bg = if (isSelected) Color(0xFF3B82F6) else Color.Transparent
                val fg = if (isSelected) Color.White else textColor

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .clickable { activeTab = tab.first }
                        .padding(vertical = 10.dp)
                        .testTag("tab_${tab.first}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tab.second,
                        fontWeight = FontWeight.Bold,
                        color = fg,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == "CHAT") {
            // --- TAB 1: DYNAMIC CHAT INTERFACE ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Scrollable Chat Message History
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isUser = msg.isUser
                        val alignment = if (isUser) Alignment.End else Alignment.Start
                        val bubbleBg = if (isUser) chatBubbleUserBg else chatBubbleAiBg
                        val textCol = if (isUser) Color.White else chatBubbleAiText

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignment
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .background(bubbleBg)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = textCol,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Loading indicator for AI generating response
                    if (aiLoading) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF3B82F6),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    "Coach is writing analysis...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Dynamic suggestion prompt chips
                val prompts = listOf(
                    "Analyze food spending",
                    "How to save for laptop?",
                    "Improve health score",
                    "Clear chat"
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    prompts.take(3).forEach { p ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkMode) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0))
                                .clickable {
                                    if (p == "Clear chat") {
                                        viewModel.clearChat()
                                    } else {
                                        viewModel.sendChatMessage(p)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("suggest_prompt_$p"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(p, fontSize = 10.sp, color = textColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Chat text input box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 100.dp), // Height of navigation bar margin
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Ask anything...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                            focusedContainerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White,
                            unfocusedContainerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
                        )
                    )

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                viewModel.sendChatMessage(chatInput.trim())
                                chatInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                            .testTag("chat_send_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        } else {
            // --- TAB 2: SMART VOICE & NATURAL LANGUAGE LOGGING ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinanceCard(isDark = isDarkMode) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            "Voice Expense Quick Logger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "AI automatically parses amounts, categories, and logs descriptions instantly in SQLite.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Sample commands to click and auto-paste
                        val samples = listOf(
                            "I spent ${currency}250 on books",
                            "Paid ${currency}1200 rent deposit by bank transfer",
                            "Scholarship money received ${currency}5000"
                        )

                        Text(
                            "Tap to try these expression examples:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        samples.forEach { sample ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkMode) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFF1F5F9))
                                    .clickable { voiceLogInput = sample }
                                    .padding(10.dp)
                                    .testTag("sample_voice_log_$sample"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    sample,
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B5CF6),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // Big Text Field for natural expressions
                OutlinedTextField(
                    value = voiceLogInput,
                    onValueChange = { voiceLogInput = it },
                    placeholder = { Text("e.g. Spent 150 on dinner UPI...") },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("voice_log_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                        focusedContainerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White,
                        unfocusedContainerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
                    )
                )

                Button(
                    onClick = {
                        if (voiceLogInput.isNotBlank()) {
                            viewModel.logTransactionViaVoiceText(voiceLogInput.trim())
                            voiceLogInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_voice_log_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !aiLoading
                ) {
                    if (aiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Parsing with AI...", color = Color.White)
                    } else {
                        Icon(Icons.Default.Bolt, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick Log Expense", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
