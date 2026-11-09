package com.example.yappy

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.adapter.ChatAdapter
import com.example.yappy.api.ChatWebSocketCallback
import com.example.yappy.api.ChatWebSocketClient
import com.example.yappy.api.ChatWebSocketConfig
import com.example.yappy.model.ChatMessage
import com.example.yappy.storage.ChatMessageStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity(), ChatWebSocketCallback {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var tvStatus: TextView
    private lateinit var wsClient: ChatWebSocketClient

    private var currentUserName: String = "You"
    private var chatName: String = "Chat"
    private var communityId: String = ""
    private var communityName: String = "Server"
    private var isTryingFallback = false

    private val storageChatKey: String
        get() = if (communityId.isNotBlank()) {
            "${communityId}_${chatName.removePrefix("#").trim()}"
        } else if (communityName.isNotBlank() && communityName != "Server" && communityName != "Community") {
            "${communityName}_${chatName.removePrefix("#").trim()}"
        } else {
            chatName
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserName = prefs.getString("name", null)
            ?: prefs.getString("email", null)?.substringBefore("@")
            ?: "You"

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.chat_root)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, maxOf(bars.top, minTop), bars.right, maxOf(bars.bottom, minBottom))
            insets
        }

        chatName = intent.getStringExtra(EXTRA_CHAT_NAME) ?: "Chat"
        val lastMessage = intent.getStringExtra(EXTRA_LAST_MESSAGE)
        communityId = intent.getStringExtra(ChannelRecordActivity.EXTRA_COMMUNITY_ID)
            ?: intent.getStringExtra("community_id").orEmpty()
        communityName = intent.getStringExtra(ChannelRecordActivity.EXTRA_COMMUNITY_NAME)
            ?: intent.getStringExtra("community_name") ?: "Server"

        val openRecordAction = View.OnClickListener {
            val recordIntent = Intent(this@ChatActivity, ChannelRecordActivity::class.java).apply {
                putExtra(ChannelRecordActivity.EXTRA_CHANNEL_NAME, chatName)
                putExtra(ChannelRecordActivity.EXTRA_COMMUNITY_ID, communityId)
                putExtra(ChannelRecordActivity.EXTRA_COMMUNITY_NAME, communityName)
                putExtra(ChannelRecordActivity.EXTRA_INITIAL_TAB, 0)
            }
            startActivity(recordIntent)
        }

        findViewById<TextView>(R.id.chat_title).apply {
            text = chatName
            setOnClickListener(openRecordAction)
        }
        findViewById<View>(R.id.btn_channel_members)?.setOnClickListener(openRecordAction)
        findViewById<TextView>(R.id.chat_back).setOnClickListener { finish() }

        tvStatus = findViewById(R.id.chat_status)
        tvStatus.setOnClickListener {
            showServerConfigDialog()
        }

        rvChat = findViewById(R.id.rv_chat)
        etMessage = findViewById(R.id.et_message)

        chatAdapter = ChatAdapter()
        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = chatAdapter

        val savedMessages = ChatMessageStore.getMessages(this, storageChatKey)
        if (savedMessages.isNotEmpty()) {
            chatAdapter.setMessages(savedMessages)
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)
        }

        if (!lastMessage.isNullOrBlank()) {
            val isFromSender = intent.getBooleanExtra(EXTRA_IS_MINE, true)
            val exists = savedMessages.any { it.text == lastMessage && it.isMine == isFromSender }
            if (!exists) {
                val newMsg = ChatMessage(
                    id = System.nanoTime(),
                    text = lastMessage,
                    isMine = isFromSender,
                    time = now(),
                    senderName = if (isFromSender) currentUserName else chatName.removePrefix("#").trim().ifEmpty { "User" }
                )
                chatAdapter.add(newMsg)
                ChatMessageStore.saveMessage(this, storageChatKey, newMsg)
                rvChat.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }

        findViewById<Button>(R.id.btn_send).setOnClickListener { sendMessage() }
        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        wsClient = ChatWebSocketClient(this)
        connectWebSocket()
    }

    private fun connectWebSocket(targetUrl: String? = null) {
        val url = targetUrl ?: ChatWebSocketConfig.getWebSocketUrl(this)
        tvStatus.text = "● Connecting..."
        tvStatus.setTextColor(Color.parseColor("#FEE75C"))
        wsClient.connect(url)
    }

    private fun showServerConfigDialog() {
        val currentUrl = ChatWebSocketConfig.getWebSocketUrl(this)
        val endpoints = arrayOf(
            "Local USB (ws://localhost:5001)",
            "Local Wi-Fi (ws://30.31.0.103:5001)",
            "Emulator (ws://10.0.2.2:5001)",
            "Production (wss://smart-community-2.onrender.com)",
            "Custom URL...",
            "Clear Chat History"
        )

        AlertDialog.Builder(this)
            .setTitle("Chat Options")
            .setItems(endpoints) { _, which ->
                when (which) {
                    0 -> {
                        ChatWebSocketConfig.setWebSocketUrl(this, ChatWebSocketConfig.DEFAULT_LOCAL_URL)
                        isTryingFallback = false
                        connectWebSocket(ChatWebSocketConfig.DEFAULT_LOCAL_URL)
                    }
                    1 -> {
                        ChatWebSocketConfig.setWebSocketUrl(this, ChatWebSocketConfig.DEFAULT_LOCAL_WIFI_URL)
                        isTryingFallback = false
                        connectWebSocket(ChatWebSocketConfig.DEFAULT_LOCAL_WIFI_URL)
                    }
                    2 -> {
                        ChatWebSocketConfig.setWebSocketUrl(this, ChatWebSocketConfig.DEFAULT_LOCAL_EMULATOR_URL)
                        isTryingFallback = false
                        connectWebSocket(ChatWebSocketConfig.DEFAULT_LOCAL_EMULATOR_URL)
                    }
                    3 -> {
                        ChatWebSocketConfig.setWebSocketUrl(this, ChatWebSocketConfig.DEFAULT_PRODUCTION_URL)
                        isTryingFallback = false
                        connectWebSocket(ChatWebSocketConfig.DEFAULT_PRODUCTION_URL)
                    }
                    4 -> {
                        showCustomUrlDialog(currentUrl)
                    }
                    5 -> {
                        ChatMessageStore.clearMessages(this, storageChatKey)
                        chatAdapter.setMessages(emptyList())
                        Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setPositiveButton("Reconnect") { _, _ ->
                isTryingFallback = false
                connectWebSocket()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomUrlDialog(currentUrl: String) {
        val input = EditText(this).apply {
            setText(currentUrl)
            setSelection(text.length)
            setSingleLine()
            setPadding(48, 36, 48, 36)
        }

        AlertDialog.Builder(this)
            .setTitle("Custom WebSocket URL")
            .setView(input)
            .setPositiveButton("Connect") { _, _ ->
                val newUrl = input.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    ChatWebSocketConfig.setWebSocketUrl(this, newUrl)
                    isTryingFallback = false
                    connectWebSocket(newUrl)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val msg = ChatMessage(
            id = System.nanoTime(),
            text = text,
            isMine = true,
            time = now(),
            senderName = currentUserName
        )
        chatAdapter.add(msg)
        ChatMessageStore.saveMessage(this, storageChatKey, msg)
        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
        etMessage.text.clear()

        val payload = "$currentUserName: $text"
        val sent = wsClient.sendMessage(payload)
        if (!sent) {
            Toast.makeText(this, "Chat server not connected. Message shown locally.", Toast.LENGTH_SHORT).show()
            connectWebSocket()
        }
    }

    override fun onConnected() {
        runOnUiThread {
            isTryingFallback = false
            tvStatus.text = "● Live Chat (Connected)"
            tvStatus.setTextColor(Color.parseColor("#57F287"))
        }
    }

    override fun onMessageReceived(text: String) {
        runOnUiThread {
            val sender: String
            val messageBody: String
            if (text.contains(": ") && !text.startsWith("http://") && !text.startsWith("https://")) {
                sender = text.substringBefore(": ").trim()
                messageBody = text.substringAfter(": ").trim()
            } else {
                sender = chatName.removePrefix("#").trim().ifEmpty { "Community Member" }
                messageBody = text
            }

            val isSelf = sender.equals(currentUserName, ignoreCase = true)
            val msg = ChatMessage(
                id = System.nanoTime(),
                text = messageBody,
                isMine = isSelf,
                time = now(),
                senderName = sender
            )
            chatAdapter.add(msg)
            ChatMessageStore.saveMessage(this@ChatActivity, storageChatKey, msg)
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            tvStatus.text = "● Disconnected (Tap to reconnect)"
            tvStatus.setTextColor(Color.parseColor("#ED4245"))
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            val currentUrl = ChatWebSocketConfig.getWebSocketUrl(this)
            if (!isTryingFallback && currentUrl == ChatWebSocketConfig.DEFAULT_LOCAL_URL) {
                isTryingFallback = true
                tvStatus.text = "● Retrying via Wi-Fi IP..."
                tvStatus.setTextColor(Color.parseColor("#FEE75C"))
                wsClient.connect(ChatWebSocketConfig.DEFAULT_LOCAL_WIFI_URL)
                return@runOnUiThread
            }
            tvStatus.text = "● Offline (Tap to config URL)"
            tvStatus.setTextColor(Color.parseColor("#ED4245"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wsClient.disconnect()
    }

    private fun now(): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

    companion object {
        const val EXTRA_CHAT_NAME = "chat_name"
        const val EXTRA_LAST_MESSAGE = "chat_last_message"
        const val EXTRA_IS_MINE = "chat_is_mine"
    }
}
