package com.liberium.ai

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Ink = Color(0xFF0B0B12)
private val Panel = Color(0xFF151521)
private val Violet = Color(0xFF9B75FF)
private val TextMain = Color(0xFFF4F1FA)
private val TextMuted = Color(0xFF9692A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LiberiumApp() }
    }
}

@Composable
fun LiberiumApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var drawerOpen by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var chatTitle by remember { mutableStateOf("Новый чат") }
    var apiKey by remember { mutableStateOf(SecureKeyStore.read(context)) }
    var showSettings by remember { mutableStateOf(false) }
    var keyDraft by remember { mutableStateOf(apiKey) }
    var sending by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(if (apiKey.isBlank()) "Нужен API-ключ" else "Groq подключается по запросу") }
    var pendingUris by remember { mutableStateOf(listOf<Uri>()) }
    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { picked ->
        pendingUris = (pendingUris + picked).distinct()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Ink,
            surface = Panel,
            primary = Violet,
            onBackground = TextMain,
            onSurface = TextMain
        )
    ) {
        Row(Modifier.fillMaxSize().background(Ink)) {
            if (drawerOpen) {
                Column(
                    Modifier.width(270.dp).fillMaxHeight().background(Panel).padding(16.dp)
                ) {
                    Text("LIBERIUM", color = Violet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("AI", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(22.dp))
                    FilledTonalButton(
                        onClick = {
                            messages = emptyList()
                            chatTitle = "Новый чат"
                            drawerOpen = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF29233B))
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Новый чат")
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("ИСТОРИЯ", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        chatTitle,
                        color = TextMain,
                        modifier = Modifier.fillMaxWidth().clickable { drawerOpen = false }.padding(vertical = 12.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Text("Liberium AI • 0.2.0", color = TextMuted, fontSize = 11.sp)
                }
            }

            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { drawerOpen = !drawerOpen }) {
                        Icon(Icons.Default.Menu, "Меню", tint = TextMain)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Liberium AI", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(status, color = TextMuted, fontSize = 11.sp)
                    }
                    IconButton(onClick = {
                        keyDraft = apiKey
                        showSettings = true
                    }) {
                        Icon(Icons.Default.Settings, "Настройки", tint = Violet)
                    }
                    Surface(shape = CircleShape, color = Color(0xFF29233B)) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Violet, modifier = Modifier.padding(10.dp).size(18.dp))
                    }
                }

                HorizontalDivider(color = Color(0xFF242331))

                if (messages.isEmpty()) {
                    Column(
                        Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF211A32)) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Violet, modifier = Modifier.padding(20.dp).size(34.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Чем займёмся?", color = TextMain, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Спроси что угодно — Liberium AI ответит.", color = TextMuted, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Модель: Groq • openai/gpt-oss-20b", color = TextMuted, fontSize = 11.sp)
                    }
                } else {
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages) { message ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = if (message.role == "user") Color(0xFF302447) else Panel,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.widthIn(max = 330.dp)
                                ) {
                                    Text(message.content, color = TextMain, modifier = Modifier.padding(14.dp), fontSize = 15.sp)
                                }
                            }
                        }
                        if (sending) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Violet, strokeWidth = 2.dp)
                                    Text("Liberium думает…", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Surface(color = Ink, shadowElevation = 8.dp) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                        if (pendingUris.isNotEmpty()) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Файлов: ${pendingUris.size} • ${pendingUris.take(2).joinToString { it.lastPathSegment?.substringAfterLast('/') ?: "вложение" }}", color = Violet, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 2)
                                TextButton(onClick = { pendingUris = emptyList() }) { Text("Очистить") }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(24.dp)).padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { attachmentPicker.launch(arrayOf("*/*")) }) {
                                Icon(Icons.Default.AttachFile, "Прикрепить файлы и фото", tint = if (pendingUris.isEmpty()) TextMuted else Violet)
                            }
                            TextField(
                                value = input,
                                onValueChange = { input = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Напиши сообщение...", color = TextMuted, fontSize = 14.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Violet
                                ),
                                maxLines = 5
                            )
                            IconButton(
                                onClick = {
                                    val text = input.trim()
                                    if (text.isNotEmpty() && !sending) {
                                        if (apiKey.isBlank()) {
                                            keyDraft = ""
                                            showSettings = true
                                        } else {
                                            val selectedUris = pendingUris.toList()
                                            val attachmentLabel = if (selectedUris.isEmpty()) "" else "\n\n[Прикреплено файлов: ${selectedUris.size}]"
                                            val updated = messages + ChatMessage("user", text + attachmentLabel)
                                            messages = updated
                                            if (chatTitle == "Новый чат") chatTitle = text.take(24)
                                            input = ""
                                            pendingUris = emptyList()
                                            sending = true
                                            status = "Запрос отправляется…"
                                            scope.launch {
                                                try {
                                                    val prepared = selectedUris.map { AttachmentProcessor.prepare(context, it) }
                                                    val answer = GroqClient.send(apiKey, updated, prepared)
                                                    messages = messages + ChatMessage("assistant", answer)
                                                    status = "Подключено • Groq"
                                                } catch (e: Exception) {
                                                    messages = messages + ChatMessage(
                                                        "assistant",
                                                        "Не удалось получить ответ: ${e.message ?: "неизвестная ошибка"}\n\nПроверь интернет, API-ключ и бесплатный лимит Groq."
                                                    )
                                                    status = "Ошибка подключения"
                                                } finally {
                                                    sending = false
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = input.isNotBlank() && !sending,
                                modifier = Modifier.background(if (input.isNotBlank() && !sending) Violet else Color(0xFF393443), CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowUpward, "Отправить", tint = Color.White)
                            }
                        }
                        Text(
                            "Фото, PDF, DOCX, TXT, код и ZIP • ограничения провайдера могут применяться",
                            color = TextMuted, fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 7.dp)
                        )
                    }
                }
            }
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Подключение AI") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Вставь свой Groq API-ключ. Он будет сохранён на телефоне в зашифрованном виде.", fontSize = 13.sp)
                        OutlinedTextField(
                            value = keyDraft,
                            onValueChange = { keyDraft = it },
                            label = { Text("Groq API key") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Получить ключ: console.groq.com/keys", color = Violet, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        apiKey = keyDraft.trim()
                        SecureKeyStore.write(context, apiKey)
                        status = if (apiKey.isBlank()) "Нужен API-ключ" else "Ключ сохранён • готов к запросу"
                        showSettings = false
                    }) { Text("Сохранить") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        keyDraft = ""
                        apiKey = ""
                        SecureKeyStore.write(context, "")
                        status = "Нужен API-ключ"
                        showSettings = false
                    }) { Text("Удалить ключ") }
                }
            )
        }
    }
}

data class ChatMessage(val role: String, val content: String)
