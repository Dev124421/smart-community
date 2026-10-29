package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yappy.api.Channel
import com.example.yappy.api.CommunityApi
import com.example.yappy.api.SessionManager
import kotlin.concurrent.thread

class Rules : AppCompatActivity() {

    private lateinit var session: SessionManager
    private var activeChannelName: String = "welcome-and-rules"
    private var channelsList: List<Channel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rules)

        session = SessionManager(this)

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(systemBars.left, maxOf(systemBars.top, minTop), systemBars.right, maxOf(systemBars.bottom, minBottom))
            insets
        }

        val communityId = intent.getStringExtra(homescreen.EXTRA_COMMUNITY_ID).orEmpty()
        val communityName = intent.getStringExtra(homescreen.EXTRA_COMMUNITY_NAME) ?: "Community"

        val tvChannelName = findViewById<TextView>(R.id.tv_channel_name)
        val tvWelcomeTitle = findViewById<TextView>(R.id.tv_welcome_title)
        val etMessage = findViewById<EditText>(R.id.et_message)

        tvChannelName?.text = "# $activeChannelName"
        tvWelcomeTitle?.text = "Welcome to $communityName's server"
        etMessage?.hint = "Message #$activeChannelName..."
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
        findViewById<View>(R.id.btn_setup_server)?.setOnClickListener {
            val intent = Intent(this, SetUpFinish::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
                putExtra("SERVER_NAME", communityName)
            }
            startActivity(intent)
        }
        etMessage?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val text = etMessage.text.toString().trim()
                if (text.isNotEmpty()) {
                    val intent = Intent(this, ChatActivity::class.java).apply {
                        putExtra(ChatActivity.EXTRA_CHAT_NAME, "# $activeChannelName")
                        putExtra(ChatActivity.EXTRA_LAST_MESSAGE, text)
                    }
                    startActivity(intent)
                    etMessage.text.clear()
                }
                true
            } else false
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
        if (communityId.isNotBlank() && !communityId.startsWith("local_")) {
            loadChannels(communityId)
        }
    }

    private fun loadChannels(communityId: String) {
        val token = session.getAccessToken().orEmpty()
        if (token.isBlank()) return

        thread {
            val result = CommunityApi.getChannels(token, communityId)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                result.onSuccess { channels ->
                    channelsList = channels
                    if (channels.isNotEmpty()) {
                        activeChannelName = channels.first().name
                        findViewById<TextView>(R.id.tv_channel_name)?.text = "# $activeChannelName"
                        findViewById<EditText>(R.id.et_message)?.hint = "Message #$activeChannelName..."
                    } else {
                        createDefaultChannel(token, communityId)
                    }
                }
            }
        }
    }

    private fun createDefaultChannel(token: String, communityId: String) {
        thread {
            val result = CommunityApi.createChannel(
                token,
                communityId,
                "general",
                "General discussion for community members"
            )
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                result.onSuccess { channel ->
                    activeChannelName = channel.name
                    findViewById<TextView>(R.id.tv_channel_name)?.text = "# $activeChannelName"
                    findViewById<EditText>(R.id.et_message)?.hint = "Message #$activeChannelName..."
                }
            }
        }
    }
}
