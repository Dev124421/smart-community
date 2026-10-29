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

    /** ids of communities the user already belongs to */
    private val memberIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomescreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        session = SessionManager(this)

        showUsername()
        setupRecyclerViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload so a community created/joined elsewhere shows up when we come back
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
        // Main list = communities you have NOT joined yet ("Join communities")
        communityAdapter = CommunityAdapter(mutableListOf()) { selected ->
            onCommunityClicked(selected)
        }
        binding.rvJoinedCommunities.apply {
            layoutManager = LinearLayoutManager(this@homescreen)
            adapter = communityAdapter
            isNestedScrollingEnabled = false
        }

        // Left rail = communities you HAVE joined
        railAdapter = ServerRailAdapter(emptyList()) { selected ->
            openCommunity(selected)
        }
        binding.rvServerRail.apply {
            layoutManager = LinearLayoutManager(this@homescreen)
            adapter = railAdapter
        }
    }

    // ------------------------------------------------------------------
    // API
    // ------------------------------------------------------------------

    private fun accessToken(): String? = session.getAccessToken()

    private fun loadCommunities() {
        val token = accessToken()
        if (token.isNullOrBlank()) {
            goToLogin()
            return
        }

        thread {
            val result = CommunityApi.getCommunities(token)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                result.onSuccess { list ->
                    val joined = list.filter { it.isMember }
                    val others = list.filter { !it.isMember }

                    memberIds.clear()
                    memberIds.addAll(joined.map { it.id })

                    railAdapter.submit(joined.map { it.toModel() })
                    communityAdapter.updateList(others.map { it.toModel() })
                }.onFailure { handleError(it) }
            }
        }
    }

    private fun joinCommunity(communityId: String, communityName: String?) {
        val token = accessToken()
        if (token.isNullOrBlank()) {
            goToLogin()
            return
        }

        thread {
            val result = CommunityApi.joinCommunity(token, communityId)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                result.onSuccess {
                    Toast.makeText(this, "Joined ${communityName ?: "community"}", Toast.LENGTH_SHORT).show()
                    loadCommunities()
                    if (communityName != null) {
                        openCommunity(communityId, communityName)
                    }
                }.onFailure { handleError(it) }
            }
        }
    }

    private fun handleError(error: Throwable) {
        if (error is ApiException && error.code == 401) {
            // Access token expired (they only live 15 minutes)
            Toast.makeText(this, "Session expired, please log in again", Toast.LENGTH_LONG).show()
            session.clear()
            goToLogin()
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

    // ------------------------------------------------------------------
    // Navigation / clicks
    // ------------------------------------------------------------------

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
        // Opened by class NAME (string) so this file compiles even though the activity
        // and the model are both called "Community". Set COMMUNITY_ACTIVITY to the exact
        // name of your community screen activity (same name as in AndroidManifest.xml).
        val intent = Intent().apply {
            setClassName(this@homescreen, COMMUNITY_ACTIVITY)
            putExtra(EXTRA_COMMUNITY_ID, id)
            putExtra(EXTRA_COMMUNITY_NAME, name)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Community screen not found: $COMMUNITY_ACTIVITY", Toast.LENGTH_LONG).show()
        }
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
                    // Assumes the last part of the link is the community id
                    joinCommunity(code, null)
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

        // TODO: change to the real class name of your community screen
        const val COMMUNITY_ACTIVITY = "com.example.yappy.Community"
    }
}