package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.yappy.adapter.ConversationAdapter
import com.example.yappy.adapter.ServerRailAdapter
import com.example.yappy.api.ApiCommunity
import com.example.yappy.api.CommunityApi
import com.example.yappy.api.SessionManager
import com.example.yappy.model.Community
import com.example.yappy.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class personalMessage : AppCompatActivity() {

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var railAdapter: ServerRailAdapter
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_message)

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, maxOf(systemBars.top, minTop), systemBars.right, maxOf(systemBars.bottom, minBottom))
            insets
        }

        session = SessionManager(this)

        showUsername()
        setupRecyclerViews()
        loadConversations()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadJoinedCommunities()
    }

    private fun showUsername() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val name = prefs.getString("name", null)
        val email = prefs.getString("email", null)
        val display = when {
            !name.isNullOrBlank() -> name
            !email.isNullOrBlank() -> email.substringBefore("@")
            else -> null
        }
        if (display != null) findViewById<TextView>(R.id.username).text = display
    }

    private fun setupRecyclerViews() {
        conversationAdapter = ConversationAdapter(mutableListOf()) { conversation ->
            openChat(conversation)
        }
        findViewById<RecyclerView>(R.id.rv_messages).apply {
            layoutManager = LinearLayoutManager(this@personalMessage)
            adapter = conversationAdapter
        }

        railAdapter = ServerRailAdapter(JoinedCommunityManager.getJoinedCommunities(this)) { community ->
            openCommunity(community.id, community.name)
        }
        findViewById<RecyclerView>(R.id.rv_server_rail).apply {
            layoutManager = LinearLayoutManager(this@personalMessage)
            adapter = railAdapter
        }
    }

    private fun loadJoinedCommunities() {
        val currentJoined = JoinedCommunityManager.getJoinedCommunities(this)
        railAdapter.submit(currentJoined)

        val token = session.getAccessToken()
        if (token.isNullOrBlank()) {
            return
        }

        thread {
            val result = CommunityApi.getCommunities(token)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                result.onSuccess { list ->
                    val allJoined = JoinedCommunityManager.syncWithBackend(this@personalMessage, list)
                    railAdapter.submit(allJoined)
                }
            }
        }
    }

    private fun ApiCommunity.toModel() = Community(
        id = id,
        name = name,
        description = description,
        onlineCount = category,
        membersCount = "$memberCount Members"
    )

    private fun loadConversations() {
        conversationAdapter.submit(
            listOf(
                Conversation("1", "Heartopia Bot", "Welcome to Yappy!", "10:30 AM"),
                Conversation("2", "BOT-99", "Hey, are you free tonight?", "Yesterday")
            )
        )
    }

    private fun openChat(conversation: Conversation) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CHAT_NAME, conversation.name)
            putExtra(ChatActivity.EXTRA_LAST_MESSAGE, conversation.lastMessage)
        })
    }

    private fun openCommunity(communityId: String, name: String) {
        val intent = Intent(this, ChannelListActivity::class.java).apply {
            putExtra(ChannelListActivity.EXTRA_COMMUNITY_ID, communityId)
            putExtra(ChannelListActivity.EXTRA_COMMUNITY_NAME, name)
        }
        startActivity(intent)
    }

    private fun showNewMessageDialog() {
        val input = EditText(this).apply {
            hint = "Who do you want to message?"
            setSingleLine()
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("New message")
            .setView(input)
            .setPositiveButton("Start chat") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                } else {
                    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    val conversation = Conversation(
                        System.currentTimeMillis().toString(), name, "Say hi ðŸ‘‹", time
                    )
                    conversationAdapter.addFirst(conversation)
                    openChat(conversation)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareInvite() {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join me on Yappy! Download the app and add me as a friend.")
        }
        startActivity(Intent.createChooser(send, "Invite friends"))
    }

    private fun goHome() {
        startActivity(Intent(this, homescreen::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }

    private fun setupListeners() {
        findViewById<View>(R.id.dm)?.setOnClickListener {
            goHome()
        }

        findViewById<View>(R.id.add)?.setOnClickListener {
            startActivity(Intent(this, CreateServer1::class.java))
        }

        findViewById<View>(R.id.community_shortcut)?.setOnClickListener {
            goHome()
        }

        findViewById<View>(R.id.add_friends)?.setOnClickListener {
            startActivity(Intent(this, AddPeople::class.java))
        }

        findViewById<View>(R.id.new_message)?.setOnClickListener { showNewMessageDialog() }

        findViewById<View>(R.id.search)?.setOnClickListener {
            Toast.makeText(this, "Search coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.notification)?.setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.export)?.setOnClickListener { shareInvite() }
        findViewById<View>(R.id.share)?.setOnClickListener { shareInvite() }

        findViewById<View>(R.id.account_schortcut)?.setOnClickListener {
            startActivity(Intent(this, Account::class.java))
        }
    }
}
