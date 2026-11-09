package com.example.yappy.storage

import android.content.Context
import com.example.yappy.model.ChatMessage
import org.json.JSONArray
import org.json.JSONObject

object ChatMessageStore {

    private const val PREFS_NAME = "yappy_chat_messages"

    fun getMessages(context: Context, chatKey: String): MutableList<ChatMessage> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(normalizeKey(chatKey), null) ?: return mutableListOf()

        val list = mutableListOf<ChatMessage>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.optLong("id", System.nanoTime()),
                        text = obj.optString("text", ""),
                        isMine = obj.optBoolean("isMine", false),
                        time = obj.optString("time", ""),
                        senderName = obj.optString("senderName", "User")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveMessage(context: Context, chatKey: String, message: ChatMessage) {
        val messages = getMessages(context, chatKey)
        messages.add(message)
        saveAll(context, chatKey, messages)
    }

    fun saveAll(context: Context, chatKey: String, messages: List<ChatMessage>) {
        val array = JSONArray()
        for (m in messages) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("text", m.text)
                put("isMine", m.isMine)
                put("time", m.time)
                put("senderName", m.senderName)
            }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(normalizeKey(chatKey), array.toString())
            .apply()
    }

    fun clearMessages(context: Context, chatKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .remove(normalizeKey(chatKey))
            .apply()
    }

    fun clearServerMessages(context: Context, communityId: String) {
        if (communityId.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefix = "chat_" + communityId.lowercase().replace(" ", "_").replace("#", "")
        val editor = prefs.edit()
        for (k in prefs.all.keys) {
            if (k.startsWith(prefix)) {
                editor.remove(k)
            }
        }
        editor.apply()
    }

    private fun normalizeKey(key: String): String =
        "chat_" + key.lowercase().replace(" ", "_").replace("#", "")
}
