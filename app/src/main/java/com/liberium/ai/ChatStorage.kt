package com.liberium.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatStorage {
    private const val PREFS = "liberium_chat_history"
    private const val KEY_MESSAGES = "messages"
    private const val MAX_STORED_MESSAGES = 300

    fun load(context: Context): List<ChatMessage> = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MESSAGES, "[]") ?: "[]"
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val role = item.optString("role")
                val content = item.optString("content")
                if ((role == "user" || role == "assistant") && content.isNotBlank()) {
                    add(ChatMessage(role, content))
                }
            }
        }
    }.getOrDefault(emptyList())

    fun save(context: Context, messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.takeLast(MAX_STORED_MESSAGES).forEach { message ->
            array.put(JSONObject().put("role", message.role).put("content", message.content))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MESSAGES, array.toString())
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_MESSAGES)
            .apply()
    }
}
