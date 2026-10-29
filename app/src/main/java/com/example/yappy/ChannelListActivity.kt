package com.example.yappy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.adapter.ChannelAdapter
import com.example.yappy.adapter.ServerRailAdapter
import com.example.yappy.api.Channel
import com.example.yappy.api.CommunityApi
import com.example.yappy.api.SessionManager
import kotlin.concurrent.thread

/**
 * Server Channel List screen (matching Discord's channel overview).
 * Displays all text and voice channels inside a community/server,
 * synchronizes with CommunityApi, and connects server navigation.
 */
class ChannelListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_COMMUNITY_ID = "COMMUNITY_ID"
        const val EXTRA_COMMUNITY_NAME = "COMMUNITY_NAME"
    }

    private lateinit var session: SessionManager
    private lateinit var railAdapter: ServerRailAdapter
    private lateinit var textChannelAdapter: ChannelAdapter
    private lateinit var voiceChannelAdapter: ChannelAdapter

    private var communityId: String = ""
    private var communityName: String = "Server"

    private var allChannels: List<Channel> = emptyList()
    private var isTextExpanded = true
    private var isVoiceExpanded = true

    private lateinit var rvTextChannels: RecyclerView
    private lateinit var rvVoiceChannels: RecyclerView
    private lateinit var ivTextChevron: ImageView
    private lateinit var ivVoiceChevron: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_channel_list)

        session = SessionManager(this)

        communityId = intent.getStringExtra(EXTRA_COMMUNITY_ID).orEmpty()
        communityName = intent.getStringExtra(EXTRA_COMMUNITY_NAME) ?: "Server"

        setupWindowInsets()
        bindViews()
        setupServerRail()
        setupChannelAdapters()
        loadChannels()
    }

    override fun onResume() {
        super.onResume()
        refreshRail()
        updateUserInfo()
    }

    private fun setupWindowInsets() {
        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                maxOf(systemBars.top, minTop),
                systemBars.right,
                maxOf(systemBars.bottom, minBottom)
            )
            insets
        }
    }

    private fun bindViews() {
        val tvServerName = findViewById<TextView>(R.id.tv_server_name)
        tvServerName.text = communityName
        findViewById<View>(R.id.layout_server_header).setOnClickListener {
            val recordIntent = Intent(this, ChannelRecordActivity::class.java).apply {
                putExtra(ChannelRecordActivity.EXTRA_CHANNEL_NAME, "welcome-and-rules")
                putExtra(ChannelRecordActivity.EXTRA_COMMUNITY_ID, communityId)
                putExtra(ChannelRecordActivity.EXTRA_COMMUNITY_NAME, communityName)
            }
            startActivity(recordIntent)
        }
        findViewById<View>(R.id.card_setup_progress).setOnClickListener {
            val setupIntent = Intent(this, SetUpFinish::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
                putExtra("SERVER_NAME", communityName)
            }
            startActivity(setupIntent)
        }
        findViewById<View>(R.id.btn_invite_members).setOnClickListener {
            val addIntent = Intent(this, AddPeople::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
                putExtra("SERVER_NAME", communityName)
            }
            startActivity(addIntent)
        }
        findViewById<View>(R.id.btn_calendar).setOnClickListener {
            Toast.makeText(this, "No upcoming events in $communityName", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.row_server_boosts).setOnClickListener {
            Toast.makeText(this, "Server Boosts: Level 1 active", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_dm).setOnClickListener {
            val intent = Intent(this, personalMessage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<View>(R.id.btn_add_server).setOnClickListener {
            startActivity(Intent(this, CreateServer1::class.java))
        }
        findViewById<View>(R.id.bottom_account_bar).setOnClickListener {
            startActivity(Intent(this, Account::class.java))
        }

        findViewById<View>(R.id.btn_notification).setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }
        val etSearch = findViewById<EditText>(R.id.et_search_channel)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChannels(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        rvTextChannels = findViewById(R.id.rv_text_channels)
        rvVoiceChannels = findViewById(R.id.rv_voice_channels)
        ivTextChevron = findViewById(R.id.iv_text_chevron)
        ivVoiceChevron = findViewById(R.id.iv_voice_chevron)

        findViewById<View>(R.id.header_text_channels).setOnClickListener {
            isTextExpanded = !isTextExpanded
            rvTextChannels.visibility = if (isTextExpanded) View.VISIBLE else View.GONE
            ivTextChevron.animate().rotation(if (isTextExpanded) 0f else -90f).setDuration(150).start()
        }

        findViewById<View>(R.id.header_voice_channels).setOnClickListener {
            isVoiceExpanded = !isVoiceExpanded
            rvVoiceChannels.visibility = if (isVoiceExpanded) View.VISIBLE else View.GONE
            ivVoiceChevron.animate().rotation(if (isVoiceExpanded) 0f else -90f).setDuration(150).start()
        }
        findViewById<View>(R.id.btn_add_text_channel).setOnClickListener {
            showAddChannelDialog(isVoice = false)
        }

        findViewById<View>(R.id.btn_add_voice_channel).setOnClickListener {
            showAddChannelDialog(isVoice = true)
        }
    }

    private fun setupServerRail() {
        val rvRail = findViewById<RecyclerView>(R.id.rv_server_rail)
        railAdapter = ServerRailAdapter(emptyList(), communityId) { clickedCommunity ->
            if (clickedCommunity.id != communityId) {
                val intent = Intent(this, ChannelListActivity::class.java).apply {
                    putExtra(EXTRA_COMMUNITY_ID, clickedCommunity.id)
                    putExtra(EXTRA_COMMUNITY_NAME, clickedCommunity.name)
                }
                startActivity(intent)
                finish()
            }
        }
        rvRail.adapter = railAdapter
    }

    private fun refreshRail() {
        val joined = JoinedCommunityManager.getJoinedCommunities(this)
        railAdapter.submit(joined, communityId)
    }

    private fun setupChannelAdapters() {
        textChannelAdapter = ChannelAdapter(emptyList(), activeChannelId = "default_welcome") { channel ->
            openChannel(channel)
        }
        rvTextChannels.adapter = textChannelAdapter

        voiceChannelAdapter = ChannelAdapter(emptyList()) { channel ->
            openVoiceChannel(channel)
        }
        rvVoiceChannels.adapter = voiceChannelAdapter
    }

    private fun loadChannels() {
        val cached = ChannelManager.getChannels(this, communityId)
        populateChannels(cached)
        val token = session.getAccessToken().orEmpty()
        ChannelManager.syncWithApi(this, token, communityId) { apiChannels ->
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    populateChannels(apiChannels)
                }
            }
        }
    }

    private fun populateChannels(channels: List<Channel>) {
        allChannels = channels
        val textList = channels.filter { !it.type.equals("voice", ignoreCase = true) }
        val voiceList = channels.filter { it.type.equals("voice", ignoreCase = true) }

        textChannelAdapter.submit(textList)
        voiceChannelAdapter.submit(voiceList)
    }

    private fun filterChannels(query: String) {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) {
            populateChannels(allChannels)
            return
        }
        val filtered = allChannels.filter { it.name.lowercase().contains(trimmed) }
        val textList = filtered.filter { !it.type.equals("voice", ignoreCase = true) }
        val voiceList = filtered.filter { it.type.equals("voice", ignoreCase = true) }

        textChannelAdapter.submit(textList)
        voiceChannelAdapter.submit(voiceList)
    }

    private fun openChannel(channel: Channel) {
        val normalized = channel.name.removePrefix("#").trim()

        if (normalized.equals("welcome-and-rules", ignoreCase = true) ||
            normalized.equals("rules", ignoreCase = true)) {
            val intent = Intent(this, Rules::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
                putExtra(homescreen.EXTRA_COMMUNITY_NAME, communityName)
            }
            startActivity(intent)
        } else {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_CHAT_NAME, "#$normalized")
                putExtra(ChatActivity.EXTRA_LAST_MESSAGE, "Welcome to #$normalized!")
            }
            startActivity(intent)
        }
    }

    private fun openVoiceChannel(channel: Channel) {
        Toast.makeText(this, "Connected to voice: ${channel.name} \uD83D\uDD0A", Toast.LENGTH_SHORT).show()
    }

    private fun showAddChannelDialog(isVoice: Boolean) {
        val channelType = if (isVoice) "Voice" else "Text"
        val input = EditText(this).apply {
            hint = if (isVoice) "Channel name (e.g. Lounge)" else "channel-name (e.g. clips)"
            setSingleLine()
            setPadding(48, 36, 48, 36)
        }

        AlertDialog.Builder(this)
            .setTitle("Create $channelType Channel")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                var name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    if (!isVoice) {
                        name = name.removePrefix("#").lowercase().replace(" ", "-")
                    }
                    val type = if (isVoice) "voice" else "text"
                    val newChannel = Channel(
                        id = "ch_${System.currentTimeMillis()}",
                        name = name,
                        description = "$channelType channel in $communityName",
                        type = type
                    )
                    ChannelManager.addChannel(this, communityId, newChannel)
                    loadChannels()
                    val token = session.getAccessToken().orEmpty()
                    if (token.isNotBlank() && communityId.isNotBlank() && !communityId.startsWith("local_")) {
                        thread {
                            CommunityApi.createChannel(token, communityId, name, newChannel.description)
                        }
                    }

                    Toast.makeText(this, "Channel #$name created!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserInfo() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val name = prefs.getString("name", null)
            ?: prefs.getString("email", null)?.substringBefore("@")
            ?: "BOT-99"
        findViewById<TextView>(R.id.tv_account_username)?.text = name
    }
}
