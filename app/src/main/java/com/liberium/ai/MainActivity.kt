package com.liberium.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var drawerOpen by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    var chatTitle by remember { mutableStateOf("Новый чат") }

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
                    Modifier.width(270.dp).fillMaxHeight()
                        .background(Panel).padding(16.dp)
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
                        modifier = Modifier.fillMaxWidth().clickable { drawerOpen = false }
                            .padding(vertical = 12.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Text("Liberium AI • 0.1.0", color = TextMuted, fontSize = 11.sp)
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
                        Text("Личный AI-помощник", color = TextMuted, fontSize = 11.sp)
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
                        Text("Я — Liberium AI. Здесь появятся наши разговоры.", color = TextMuted, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages) { (body, fromUser) ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = if (fromUser) Color(0xFF302447) else Panel,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.widthIn(max = 310.dp)
                                ) {
                                    Text(body, color = TextMain, modifier = Modifier.padding(14.dp), fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                Surface(color = Ink, shadowElevation = 8.dp) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Row(
                            Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(24.dp)).padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.AttachFile, "Прикрепить файл", tint = TextMuted)
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
                                    val value = input.trim()
                                    if (value.isNotEmpty()) {
                                        messages = messages + (value to true) +
                                            ("Основа приложения готова. Подключение AI появится на следующем этапе." to false)
                                        if (chatTitle == "Новый чат") chatTitle = value.take(24)
                                        input = ""
                                    }
                                },
                                enabled = input.isNotBlank(),
                                modifier = Modifier.background(
                                    if (input.isNotBlank()) Violet else Color(0xFF393443),
                                    CircleShape
                                )
                            ) {
                                Icon(Icons.Default.ArrowUpward, "Отправить", tint = Color.White)
                            }
                        }
                        Text(
                            "Ответы пока демонстрационные — AI подключим на следующем этапе.",
                            color = TextMuted, fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 7.dp)
                        )
                    }
                }
            }
        }
    }
}
