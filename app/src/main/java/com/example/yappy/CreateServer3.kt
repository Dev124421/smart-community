package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yappy.api.CommunityApi
import com.example.yappy.api.SessionManager
import kotlin.concurrent.thread

class CreateServer3 : AppCompatActivity() {

    private var selectedIcon = R.drawable.add_icon

    private var serverTemplate: String = "Custom"
    private var serverType: String = "Not specified"
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_server3)

        session = SessionManager(this)

        serverTemplate = intent.getStringExtra("SERVER_TEMPLATE") ?: "Custom"
        serverType = intent.getStringExtra("SERVER_TYPE") ?: "Not specified"

        val backButton = findViewById<ImageView>(R.id.back_arrow)
        val addIcon = findViewById<ImageView>(R.id.add_icon)
        val gamingIcon = findViewById<ImageView>(R.id.gaming_icon)
        val serverName = findViewById<EditText>(R.id.server_name)
        val createButton = findViewById<Button>(R.id.createBtn)
        val guidelines = findViewById<TextView>(R.id.guidelines)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton.setOnClickListener { finish() }

        addIcon.setOnClickListener {
            selectedIcon = R.drawable.add_icon
            Toast.makeText(this, "Default server icon selected", Toast.LENGTH_SHORT).show()
        }

        gamingIcon.setOnClickListener {
            selectedIcon = R.drawable.gaming2
            Toast.makeText(this, "Gaming icon selected", Toast.LENGTH_SHORT).show()
        }

        guidelines.setOnClickListener {
            val intent = Intent(this, Rules::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_NAME, "Community Guidelines")
            }
            startActivity(intent)
        }

        createButton.setOnClickListener {
            val name = serverName.text.toString().trim()

            if (name.isEmpty()) {
                serverName.error = "Please enter a server name"
                serverName.requestFocus()
                return@setOnClickListener
            }

            createButton.isEnabled = false
            createButton.text = "Creating Server..."

            val token = session.getAccessToken().orEmpty()
            val description = "A $serverTemplate server for $serverType"

            if (token.isBlank()) {
                val localId = "local_${System.currentTimeMillis()}"
                JoinedCommunityManager.addJoinedCommunity(
                    this,
                    id = localId,
                    name = name,
                    description = description,
                    category = serverTemplate,
                    memberCount = "1 Member"
                )
                ChannelManager.saveChannels(this, localId, ChannelManager.getDefaultChannels())
                Toast.makeText(this, "Server '$name' created successfully!", Toast.LENGTH_SHORT).show()

                val channelIntent = Intent(this, ChannelListActivity::class.java).apply {
                    putExtra(ChannelListActivity.EXTRA_COMMUNITY_ID, localId)
                    putExtra(ChannelListActivity.EXTRA_COMMUNITY_NAME, name)
                }
                startActivity(channelIntent)
                finish()
                return@setOnClickListener
            }

            thread {
                val result = CommunityApi.createCommunity(token, name, description)
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    createButton.isEnabled = true
                    createButton.text = "Create Server"

                    result.onSuccess { createdCommunity ->
                        JoinedCommunityManager.addJoinedCommunity(
                            this,
                            id = createdCommunity.id,
                            name = createdCommunity.name,
                            description = createdCommunity.description,
                            category = createdCommunity.category,
                            memberCount = "1 Member"
                        )
                        ChannelManager.saveChannels(this, createdCommunity.id, ChannelManager.getDefaultChannels())
                        thread {
                            CommunityApi.createChannel(token, createdCommunity.id, "general", "General discussion")
                            CommunityApi.createChannel(token, createdCommunity.id, "welcome-and-rules", "Server rules and announcements")
                        }

                        Toast.makeText(this, "Server '${createdCommunity.name}' created successfully!", Toast.LENGTH_SHORT).show()
                        val channelIntent = Intent(this, ChannelListActivity::class.java).apply {
                            putExtra(ChannelListActivity.EXTRA_COMMUNITY_ID, createdCommunity.id)
                            putExtra(ChannelListActivity.EXTRA_COMMUNITY_NAME, createdCommunity.name)
                        }
                        startActivity(channelIntent)
                        finish()
                    }.onFailure { error ->
                        val localId = "local_${System.currentTimeMillis()}"
                        JoinedCommunityManager.addJoinedCommunity(
                            this,
                            id = localId,
                            name = name,
                            description = description,
                            category = serverTemplate,
                            memberCount = "1 Member"
                        )
                        ChannelManager.saveChannels(this, localId, ChannelManager.getDefaultChannels())
                        Toast.makeText(this, "Server created: ${error.message}", Toast.LENGTH_SHORT).show()
                        val channelIntent = Intent(this, ChannelListActivity::class.java).apply {
                            putExtra(ChannelListActivity.EXTRA_COMMUNITY_ID, localId)
                            putExtra(ChannelListActivity.EXTRA_COMMUNITY_NAME, name)
                        }
                        startActivity(channelIntent)
                        finish()
                    }
                }
            }
        }
    }
}
