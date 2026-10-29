package com.example.yappy

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yappy.adapter.CommunityAdapter
import com.example.yappy.adapter.ServerRailAdapter
import com.example.yappy.api.ApiCommunity
import com.example.yappy.api.ApiException
import com.example.yappy.api.CommunityApi
import com.example.yappy.api.SessionManager
import com.example.yappy.databinding.ActivityHomescreenBinding
import com.example.yappy.model.Community
import kotlin.concurrent.thread

class homescreen : AppCompatActivity() {

    private lateinit var binding: ActivityHomescreenBinding
    private lateinit var communityAdapter: CommunityAdapter
    private lateinit var railAdapter: ServerRailAdapter
    private lateinit var session: SessionManager

    private val memberIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomescreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, maxOf(systemBars.top, minTop), systemBars.right, maxOf(systemBars.bottom, minBottom))
            insets
        }

        session = SessionManager(this)

        showUsername()
        setupRecyclerViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        val localJoined = JoinedCommunityManager.getJoinedCommunities(this)
        railAdapter.submit(localJoined)
        loadCommunities()
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
        if (display != null) binding.tvUsername.text = display
    }

    private fun setupRecyclerViews() {
        communityAdapter = CommunityAdapter(mutableListOf()) { selected ->
            onCommunityClicked(selected)
        }
        binding.rvJoinedCommunities.apply {
            layoutManager = LinearLayoutManager(this@homescreen)
            adapter = communityAdapter
            isNestedScrollingEnabled = false
        }

        railAdapter = ServerRailAdapter(JoinedCommunityManager.getJoinedCommunities(this)) { selected ->
            openCommunity(selected)
        }
        binding.rvServerRail.apply {
            layoutManager = LinearLayoutManager(this@homescreen)
            adapter = railAdapter
        }
    }

    private fun accessToken(): String? = session.getAccessToken()

    private fun loadCommunities() {
        val currentJoined = JoinedCommunityManager.getJoinedCommunities(this)
        railAdapter.submit(currentJoined)

        val token = accessToken()
        if (token.isNullOrBlank()) {
            loadPreviewCommunities()
            return
        }

        thread {
            val result = CommunityApi.getCommunities(token)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                result.onSuccess { list ->
                    if (list.isEmpty()) {
                        loadPreviewCommunities()
                    } else {
                        val allJoined = JoinedCommunityManager.syncWithBackend(this@homescreen, list)
                        railAdapter.submit(allJoined)

                        memberIds.clear()
                        memberIds.addAll(allJoined.map { it.id })

                        val unjoined = list.filter { apiComm ->
                            !JoinedCommunityManager.isJoined(this@homescreen, apiComm.id, apiComm.name)
                        }
                        communityAdapter.updateList(unjoined.map { it.toModel() })
                    }
                }.onFailure { 
                    loadPreviewCommunities()
                }
            }
        }
    }

    private fun loadPreviewCommunities() {
        val joined = JoinedCommunityManager.getJoinedCommunities(this)
        memberIds.clear()
        memberIds.addAll(joined.map { it.id })
        railAdapter.submit(joined)

        val defaultOthers = listOf(
            Community("3", "Study Lounge", "Collaborate on homework and projects", "Education", "12K Members"),
            Community("4", "Anime & Manga", "Discuss latest episodes and releases", "Entertainment", "88K Members"),
            Community("5", "Dev Hangout", "Kotlin, Android & Full Stack discussions", "Tech", "34K Members")
        ).filter { !JoinedCommunityManager.isJoined(this, it.id, it.name) }

        communityAdapter.updateList(defaultOthers)
    }

    private fun joinCommunity(communityId: String, communityName: String?) {
        val targetName = communityName ?: "Community $communityId"

        JoinedCommunityManager.addJoinedCommunity(
            this,
            id = communityId,
            name = targetName,
            memberCount = "Joined"
        )
        val updated = JoinedCommunityManager.getJoinedCommunities(this)
        railAdapter.submit(updated)

        Toast.makeText(this, "Joined $targetName!", Toast.LENGTH_SHORT).show()

        val token = accessToken()
        if (!token.isNullOrBlank()) {
            thread {
                CommunityApi.joinCommunity(token, communityId)
            }
        }

        openCommunity(communityId, targetName)
    }

    private fun handleError(error: Throwable) {
        if (error is ApiException && error.code == 401) {
            Toast.makeText(this, "Showing preview communities", Toast.LENGTH_SHORT).show()
            loadPreviewCommunities()
        } else {
            Toast.makeText(this, error.message ?: "Something went wrong", Toast.LENGTH_LONG).show()
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun ApiCommunity.toModel() = Community(
        id = id,
        name = name,
        description = description,
        onlineCount = category,                 // API has no "online" count, so show the category
        membersCount = "$memberCount Members",
    )

    private fun onCommunityClicked(community: Community) {
        if (community.id in memberIds) {
            openCommunity(community)
        } else {
            joinCommunity(community.id, community.name)
        }
    }

    private fun openCommunity(community: Community) {
        openCommunity(community.id, community.name)
    }

    private fun openCommunity(id: String, name: String) {
        val intent = Intent(this, ChannelListActivity::class.java).apply {
            putExtra(ChannelListActivity.EXTRA_COMMUNITY_ID, id)
            putExtra(ChannelListActivity.EXTRA_COMMUNITY_NAME, name)
        }
        startActivity(intent)
    }

    private fun showInviteDialog() {
        val input = EditText(this).apply {
            hint = "Paste invite link or community ID"
            setSingleLine()
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Join with invite link")
            .setView(input)
            .setPositiveButton("Join") { _, _ ->
                val code = input.text.toString().trim().trimEnd('/').substringAfterLast('/')
                if (code.isEmpty()) {
                    Toast.makeText(this, "Please enter an invite link", Toast.LENGTH_SHORT).show()
                } else {
                    joinCommunity(code, "Community $code")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnCreateServer.setOnClickListener {
            startActivity(Intent(this, CreateServer1::class.java))
        }

        binding.btnAddServer.setOnClickListener {
            startActivity(Intent(this, CreateServer1::class.java))
        }

        binding.btnInviteLink.setOnClickListener {
            showInviteDialog()
        }

        binding.btnDm.setOnClickListener {
            startActivity(Intent(this, personalMessage::class.java))
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }

        binding.bottomAccountBar.setOnClickListener {
            startActivity(Intent(this, Account::class.java))
        }
    }

    companion object {
        const val EXTRA_COMMUNITY_ID = "community_id"
        const val EXTRA_COMMUNITY_NAME = "community_name"
    }
}