package com.liberium.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object GroqClient {
    private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "openai/gpt-oss-20b"

    suspend fun send(apiKey: String, history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Сначала добавь API-ключ в настройках." }

        val messages = JSONArray()
        messages.put(JSONObject()
            .put("role", "system")
            .put("content", "You are Liberium AI, a helpful, honest and capable assistant. Reply in the user's language. Be clear, practical, and never claim to have performed actions you did not perform."))
        history.takeLast(24).forEach { item ->
            messages.put(JSONObject().put("role", item.role).put("content", item.content))
        }

        val body = JSONObject()
            .put("model", MODEL)
            .put("messages", messages)
            .put("temperature", 0.7)
            .put("max_tokens", 2048)

        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 90000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) {
                val detail = runCatching {
                    JSONObject(response).optJSONObject("error")?.optString("message")
                }.getOrNull().orEmpty()
                throw IllegalStateException(
                    when (code) {
                        401 -> "API-ключ неверный или отозван (401)."
                        429 -> "Бесплатный лимит или частота запросов исчерпаны (429). Попробуй позже."
                        else -> "Groq вернул HTTP $code. ${detail.ifBlank { response.take(300) }}"
                    }
                )
            }
            val json = JSONObject(response)
            json.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Провайдер вернул пустой ответ.")
        } finally {
            connection.disconnect()
        }
    }
}
