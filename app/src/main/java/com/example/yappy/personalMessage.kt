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
import com.example.yappy.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class personalMessage : AppCompatActivity() {

    private lateinit var conversationAdapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_message)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        showUsername()
        setupRecyclerView()
        loadConversations()
        setupListeners()
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

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(mutableListOf()) { conversation ->
            openChat(conversation)
        }
        findViewById<RecyclerView>(R.id.rv_messages).apply {
            layoutManager = LinearLayoutManager(this@personalMessage)
            adapter = conversationAdapter
        }
    }

    /** TODO (API): replace with your real conversations/friends endpoint. */
    private fun loadConversations() {
        conversationAdapter.submit(
            listOf(
                Conversation("1", "Heartopia Bot", "Welcome to Yappy!", "10:30 AM"),
                Conversation("2", "Swasti", "Hey, are you free tonight?", "Yesterday")
            )
        )
    }

    private fun openChat(conversation: Conversation) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CHAT_NAME, conversation.name)
            putExtra(ChatActivity.EXTRA_LAST_MESSAGE, conversation.lastMessage)
        })
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
                        System.currentTimeMillis().toString(), name, "Say hi 👋", time
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
        // Left rail: server icons go back to the home screen
        val goHomeClick = View.OnClickListener { goHome() }
        findViewById<View>(R.id.community_shortcut).setOnClickListener(goHomeClick)
        findViewById<View>(R.id.SS).setOnClickListener(goHomeClick)
        findViewById<View>(R.id.moba).setOnClickListener(goHomeClick)
        findViewById<View>(R.id.B).setOnClickListener(goHomeClick)

        findViewById<View>(R.id.add).setOnClickListener {
            startActivity(Intent(this, CreateServer1::class.java))
        }

        findViewById<View>(R.id.add_friends).setOnClickListener {
            startActivity(Intent(this, AddPeople::class.java))
        }

        findViewById<View>(R.id.new_message).setOnClickListener { showNewMessageDialog() }

        findViewById<View>(R.id.search).setOnClickListener {
            Toast.makeText(this, "Search coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.notification).setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.export).setOnClickListener { shareInvite() }
        findViewById<View>(R.id.share).setOnClickListener { shareInvite() }

        findViewById<View>(R.id.account_schortcut).setOnClickListener {
            startActivity(Intent(this, Account::class.java))
        }
    }
}