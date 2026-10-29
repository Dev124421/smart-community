package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.adapter.ChatAdapter
import com.example.yappy.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private var currentUserName: String = "You"

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

        val chatName = intent.getStringExtra(EXTRA_CHAT_NAME) ?: "Chat"
        val lastMessage = intent.getStringExtra(EXTRA_LAST_MESSAGE)

        findViewById<TextView>(R.id.chat_title).apply {
            text = chatName
            setOnClickListener {
                val recordIntent = Intent(this@ChatActivity, ChannelRecordActivity::class.java).apply {
                    putExtra(ChannelRecordActivity.EXTRA_CHANNEL_NAME, chatName)
                }
                startActivity(recordIntent)
            }
        }
        findViewById<TextView>(R.id.chat_back).setOnClickListener { finish() }

        rvChat = findViewById(R.id.rv_chat)
        etMessage = findViewById(R.id.et_message)

        chatAdapter = ChatAdapter()
        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = chatAdapter

        if (!lastMessage.isNullOrBlank()) {
            val otherSender = chatName.removePrefix("#").trim().ifEmpty { "User" }
            chatAdapter.add(
                ChatMessage(
                    id = System.nanoTime(),
                    text = lastMessage,
                    isMine = false,
                    time = now(),
                    senderName = otherSender
                )
            )
        }

        findViewById<Button>(R.id.btn_send).setOnClickListener { sendMessage() }
        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        chatAdapter.add(
            ChatMessage(
                id = System.nanoTime(),
                text = text,
                isMine = true,
                time = now(),
                senderName = currentUserName
            )
        )
        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
        etMessage.text.clear()
    }

    private fun now(): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

    companion object {
        const val EXTRA_CHAT_NAME = "chat_name"
        const val EXTRA_LAST_MESSAGE = "chat_last_message"
    }
}
