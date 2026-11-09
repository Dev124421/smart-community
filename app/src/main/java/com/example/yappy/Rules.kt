package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.adapter.ChatAdapter
import com.example.yappy.api.Channel
import com.example.yappy.api.ChatWebSocketCallback
import com.example.yappy.api.ChatWebSocketClient
import com.example.yappy.api.ChatWebSocketConfig
import com.example.yappy.api.CommunityApi
import com.example.yappy.api.SessionManager
import com.example.yappy.model.ChatMessage
import com.example.yappy.storage.ChatMessageStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class Rules : AppCompatActivity(), ChatWebSocketCallback {

    private lateinit var session: SessionManager
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var wsClient: ChatWebSocketClient

    private var activeChannelName: String = "Welcome-and-rules"
    private var communityId: String = ""
    private var communityName: String = "Community"
    private var currentUserName: String = "You"
    private var channelsList: List<Channel> = emptyList()

    private val chatKey: String
        get() = if (communityId.isNotBlank()) "${communityId}_$activeChannelName" else activeChannelName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rules)

        session = SessionManager(this)

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserName = prefs.getString("name", null)
            ?: prefs.getString("email", null)?.substringBefore("@")
            ?: "You"

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        val rulesRoot = findViewById<View>(R.id.rules_root) ?: findViewById(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rulesRoot) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(
                systemBars.left,
                maxOf(systemBars.top, minTop),
                systemBars.right,
                maxOf(systemBars.bottom, minBottom)
            )
            insets
        }

        communityId = intent.getStringExtra(homescreen.EXTRA_COMMUNITY_ID).orEmpty()
        communityName = intent.getStringExtra(homescreen.EXTRA_COMMUNITY_NAME) ?: "Community"

        val channelNameExtra = intent.getStringExtra(ChannelRecordActivity.EXTRA_CHANNEL_NAME)
            ?: intent.getStringExtra("EXTRA_CHANNEL_NAME")
            ?: intent.getStringExtra("channel_name")

        if (!channelNameExtra.isNullOrBlank()) {
            activeChannelName = channelNameExtra.removePrefix("#").trim()
        } else {
            activeChannelName = "Welcome-and-rules"
        }

        val tvChannelName = findViewById<TextView>(R.id.tv_channel_name)
        etMessage = findViewById(R.id.et_message)
        rvChat = findViewById(R.id.rv_chat)

        // Setup Chat RecyclerView
        chatAdapter = ChatAdapter()
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // Load existing messages for this channel
        loadSavedMessages()

        updateChannelUI(activeChannelName, communityName)

        val openRecordAction = View.OnClickListener {
            val recordIntent = Intent(this, ChannelRecordActivity::class.java).apply {
                putExtra(ChannelRecordActivity.EXTRA_CHANNEL_NAME, activeChannelName)
                putExtra(ChannelRecordActivity.EXTRA_COMMUNITY_ID, communityId)
                putExtra(ChannelRecordActivity.EXTRA_COMMUNITY_NAME, communityName)
            }
            startActivity(recordIntent)
        }
        tvChannelName?.setOnClickListener(openRecordAction)
        findViewById<View>(R.id.layout_channel_title)?.setOnClickListener(openRecordAction)
        findViewById<View>(R.id.iv_channel_avatar)?.setOnClickListener(openRecordAction)
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_search)?.setOnClickListener {
            Toast.makeText(this, "Search in $communityName", Toast.LENGTH_SHORT).show()
        }

        // Set Up Server Button remains above chat and opens Setup finish
        findViewById<View>(R.id.btn_setup_server)?.setOnClickListener {
            val intent = Intent(this, SetUpFinish::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
                putExtra("SERVER_NAME", communityName)
            }
            startActivity(intent)
        }

        // Focus message box when clicking anywhere on the bottom chat bar (no redirect)
        findViewById<View>(R.id.bottom_chat_bar)?.setOnClickListener {
            etMessage.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(etMessage, 0)
        }

        // Send button
        findViewById<View>(R.id.btn_send)?.setOnClickListener {
            sendMessage()
        }

        // Send via keyboard action
        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        // Scroll to bottom when focus changes to etMessage
        etMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollToBottom()
            }
        }

        findViewById<ImageView>(R.id.btn_attach)?.setOnClickListener {
            Toast.makeText(this, "Attachment clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_gift)?.setOnClickListener {
            Toast.makeText(this, "Gifts feature coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_emoji)?.setOnClickListener {
            Toast.makeText(this, "Emoji picker", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_mic)?.setOnClickListener {
            Toast.makeText(this, "Voice note recording", Toast.LENGTH_SHORT).show()
        }

        // Live Chat WebSocket
        wsClient = ChatWebSocketClient(this)
        connectWebSocket()

        if (communityId.isNotBlank() && !communityId.startsWith("local_")) {
            loadChannels(communityId, communityName)
        }
    }

    private fun loadSavedMessages() {
        val saved = ChatMessageStore.getMessages(this, chatKey)
        if (saved.isNotEmpty()) {
            chatAdapter.setMessages(saved)
            scrollToBottom()
        }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val msg = ChatMessage(
            id = System.nanoTime(),
            text = text,
            isMine = true,
            time = now,
            senderName = currentUserName
        )
        chatAdapter.add(msg)
        ChatMessageStore.saveMessage(this, chatKey, msg)
        etMessage.text.clear()
        scrollToBottom()

        val payload = "$currentUserName: $text"
        val sent = wsClient.sendMessage(payload)
        if (!sent) {
            connectWebSocket()
        }
    }

    private fun scrollToBottom() {
        findViewById<NestedScrollView>(R.id.scroll_chat_container)?.post {
            findViewById<NestedScrollView>(R.id.scroll_chat_container)?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun connectWebSocket(targetUrl: String? = null) {
        val url = targetUrl ?: ChatWebSocketConfig.getWebSocketUrl(this)
        wsClient.connect(url)
    }

    override fun onConnected() {
        // Connected to chat server
    }

    override fun onMessageReceived(text: String) {
        runOnUiThread {
            if (isFinishing || isDestroyed) return@runOnUiThread
            val sender: String
            val messageBody: String
            if (text.contains(": ") && !text.startsWith("http://") && !text.startsWith("https://")) {
                sender = text.substringBefore(": ").trim()
                messageBody = text.substringAfter(": ").trim()
            } else {
                sender = activeChannelName.removePrefix("#").trim().ifEmpty { "Community Member" }
                messageBody = text
            }

            val isSelf = sender.equals(currentUserName, ignoreCase = true)
            val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            val msg = ChatMessage(
                id = System.nanoTime(),
                text = messageBody,
                isMine = isSelf,
                time = now,
                senderName = sender
            )
            chatAdapter.add(msg)
            ChatMessageStore.saveMessage(this, chatKey, msg)
            scrollToBottom()
        }
    }

    override fun onDisconnected() {
        // Connection closed
    }

    override fun onError(error: String) {
        // Connection error
    }

    override fun onDestroy() {
        super.onDestroy()
        wsClient.disconnect()
    }

    private fun updateChannelUI(name: String, communityName: String) {
        activeChannelName = name
        findViewById<TextView>(R.id.tv_channel_name)?.text = "# $activeChannelName >"
        findViewById<EditText>(R.id.et_message)?.hint = "Message #$activeChannelName"
        findViewById<TextView>(R.id.tv_welcome_title)?.text = "Welcome to #$activeChannelName!"
        findViewById<TextView>(R.id.tv_welcome_subtitle)?.text =
            "This is the start of the #$activeChannelName channel in $communityName."
    }

    private fun loadChannels(communityId: String, communityName: String) {
        val token = session.getAccessToken().orEmpty()
        if (token.isBlank()) return

        thread {
            val result = CommunityApi.getChannels(token, communityId)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                result.onSuccess { channels ->
                    channelsList = channels
                    val matched = channels.find { it.name.equals(activeChannelName, ignoreCase = true) }
                    if (matched != null) {
                        updateChannelUI(matched.name, communityName)
                    } else if (activeChannelName.equals("Welcome-and-rules", ignoreCase = true)) {
                        val welcomeCh = channels.find {
                            it.name.contains("welcome", ignoreCase = true) || it.name.contains("rule", ignoreCase = true)
                        }
                        if (welcomeCh != null) {
                            updateChannelUI(welcomeCh.name, communityName)
                        } else {
                            updateChannelUI(activeChannelName, communityName)
                        }
                    } else if (channels.isEmpty()) {
                        createDefaultChannel(token, communityId, communityName)
                    } else {
                        updateChannelUI(activeChannelName, communityName)
                    }
                }
            }
        }
    }

    private fun createDefaultChannel(token: String, communityId: String, communityName: String) {
        thread {
            val result = CommunityApi.createChannel(
                token,
                communityId,
                "Welcome-and-rules",
                "Welcome and server guidelines"
            )
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                result.onSuccess { channel ->
                    updateChannelUI(channel.name, communityName)
                }
            }
        }
    }
}
