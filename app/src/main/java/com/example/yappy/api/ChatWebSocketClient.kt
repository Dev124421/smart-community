package com.example.yappy.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

interface ChatWebSocketCallback {
    fun onConnected()
    fun onMessageReceived(text: String)
    fun onDisconnected()
    fun onError(error: String)
}

object ChatWebSocketConfig {
    const val DEFAULT_LOCAL_URL = "ws://localhost:5001"
    const val DEFAULT_LOCAL_WIFI_URL = "ws://30.31.0.103:5001"
    const val DEFAULT_LOCAL_EMULATOR_URL = "ws://10.0.2.2:5001"
    const val DEFAULT_PRODUCTION_URL = "wss://smart-community-2.onrender.com"

    fun getWebSocketUrl(context: Context): String {
        val prefs = context.getSharedPreferences("chat_ws_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("chat_ws_url", null)
        if (saved == null || saved == DEFAULT_PRODUCTION_URL) {
            return DEFAULT_LOCAL_URL
        }
        return saved
    }

    fun setWebSocketUrl(context: Context, url: String) {
        context.getSharedPreferences("chat_ws_prefs", Context.MODE_PRIVATE).edit()
            .putString("chat_ws_url", url)
            .apply()
    }
}

class ChatWebSocketClient(private val callback: ChatWebSocketCallback) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    var isConnected: Boolean = false
        private set

    fun connect(url: String) {
        disconnect()

        val request = try {
            Request.Builder().url(url).build()
        } catch (e: Exception) {
            callback.onError("Invalid URL: ${e.message}")
            return
        }

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                callback.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                callback.onMessageReceived(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                callback.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                callback.onError(t.message ?: "Connection error")
            }
        })
    }

    fun sendMessage(message: String): Boolean {
        return if (isConnected && webSocket != null) {
            webSocket?.send(message) == true
        } else {
            false
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Normal closure")
        } catch (_: Exception) {
        }
        webSocket = null
        isConnected = false
    }
}
