package com.example.yappy

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChannelRecordActivity : AppCompatActivity() {

    private var activeTabIndex = 0

    private lateinit var tvTabMembers: TextView
    private lateinit var tvTabMedia: TextView
    private lateinit var tvTabThreads: TextView
    private lateinit var tvTabLinks: TextView
    private lateinit var tvTabFiles: TextView

    private lateinit var indMembers: View
    private lateinit var indMedia: View
    private lateinit var indThreads: View
    private lateinit var indLinks: View
    private lateinit var indFiles: View

    private lateinit var viewMembers: View
    private lateinit var viewMedia: View
    private lateinit var viewThreads: View
    private lateinit var viewLinks: View
    private lateinit var viewFiles: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_channel_record)

        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.channel_record_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, maxOf(systemBars.top, minTop), systemBars.right, maxOf(systemBars.bottom, minBottom))
            insets
        }

        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: "welcome-and-rules"
        val communityId = intent.getStringExtra(EXTRA_COMMUNITY_ID).orEmpty()
        val communityName = intent.getStringExtra(EXTRA_COMMUNITY_NAME) ?: "Server"
        val initialTab = intent.getIntExtra(EXTRA_INITIAL_TAB, 0)

        findViewById<TextView>(R.id.tv_channel_name)?.text = channelName.removePrefix("#").trim()

        bindViews()
        setupTabClicks()
        setupHeaderActions(channelName, communityId, communityName)
        setupTabInteractions(communityId, communityName)

        selectTab(initialTab.coerceIn(0, 4))
    }

    private fun bindViews() {
        tvTabMembers = findViewById(R.id.tv_tab_members)
        tvTabMedia = findViewById(R.id.tv_tab_media)
        tvTabThreads = findViewById(R.id.tv_tab_threads)
        tvTabLinks = findViewById(R.id.tv_tab_links)
        tvTabFiles = findViewById(R.id.tv_tab_files)

        indMembers = findViewById(R.id.indicator_members)
        indMedia = findViewById(R.id.indicator_media)
        indThreads = findViewById(R.id.indicator_threads)
        indLinks = findViewById(R.id.indicator_links)
        indFiles = findViewById(R.id.indicator_files)

        viewMembers = findViewById(R.id.view_tab_members)
        viewMedia = findViewById(R.id.view_tab_media)
        viewThreads = findViewById(R.id.view_tab_threads)
        viewLinks = findViewById(R.id.view_tab_links)
        viewFiles = findViewById(R.id.view_tab_files)
    }

    private fun setupTabClicks() {
        findViewById<View>(R.id.tab_members)?.setOnClickListener { selectTab(0) }
        findViewById<View>(R.id.tab_media)?.setOnClickListener { selectTab(1) }
        findViewById<View>(R.id.tab_threads)?.setOnClickListener { selectTab(2) }
        findViewById<View>(R.id.tab_links)?.setOnClickListener { selectTab(3) }
        findViewById<View>(R.id.tab_files)?.setOnClickListener { selectTab(4) }
    }

    private fun selectTab(index: Int) {
        activeTabIndex = index

        val activePink = getColor(R.color.pink)
        val mutedGrey = getColor(R.color.soft_border)

        tvTabMembers.setTextColor(if (index == 0) activePink else mutedGrey)
        tvTabMedia.setTextColor(if (index == 1) activePink else mutedGrey)
        tvTabThreads.setTextColor(if (index == 2) activePink else mutedGrey)
        tvTabLinks.setTextColor(if (index == 3) activePink else mutedGrey)
        tvTabFiles.setTextColor(if (index == 4) activePink else mutedGrey)

        indMembers.visibility = if (index == 0) View.VISIBLE else View.INVISIBLE
        indMedia.visibility = if (index == 1) View.VISIBLE else View.INVISIBLE
        indThreads.visibility = if (index == 2) View.VISIBLE else View.INVISIBLE
        indLinks.visibility = if (index == 3) View.VISIBLE else View.INVISIBLE
        indFiles.visibility = if (index == 4) View.VISIBLE else View.INVISIBLE

        viewMembers.visibility = if (index == 0) View.VISIBLE else View.GONE
        viewMedia.visibility = if (index == 1) View.VISIBLE else View.GONE
        viewThreads.visibility = if (index == 2) View.VISIBLE else View.GONE
        viewLinks.visibility = if (index == 3) View.VISIBLE else View.GONE
        viewFiles.visibility = if (index == 4) View.VISIBLE else View.GONE
    }

    private fun setupHeaderActions(channelName: String, communityId: String, communityName: String) {
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btn_search)?.setOnClickListener {
            Toast.makeText(this, "Search in #$channelName", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_notification)?.setOnClickListener {
            Toast.makeText(this, "Channel notifications enabled", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_settings)?.setOnClickListener {
            Toast.makeText(this, "Channel settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabInteractions(communityId: String, communityName: String) {
        viewMembers.findViewById<View>(R.id.card_setup_server)?.setOnClickListener {
            val intent = Intent(this, SetUpFinish::class.java).apply {
                putExtra(homescreen.EXTRA_COMMUNITY_ID, communityId)
                putExtra("SERVER_NAME", communityName)
            }
            startActivity(intent)
        }

        viewMembers.findViewById<View>(R.id.card_member_item)?.setOnClickListener {
            val memberName = viewMembers.findViewById<TextView>(R.id.tv_member_name)?.text ?: "Member"
            Toast.makeText(this, "$memberName is active in #welcome-and-rules", Toast.LENGTH_SHORT).show()
        }

        viewThreads.findViewById<View>(R.id.btn_create_thread)?.setOnClickListener {
            showCreateThreadDialog()
        }
    }

    private fun showCreateThreadDialog() {
        val input = EditText(this).apply {
            hint = "Thread title"
            setSingleLine()
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Create a Thread")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    Toast.makeText(this, "Thread '$title' created!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a thread title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        const val EXTRA_CHANNEL_NAME = "channel_name"
        const val EXTRA_COMMUNITY_ID = "community_id"
        const val EXTRA_COMMUNITY_NAME = "community_name"
        const val EXTRA_INITIAL_TAB = "initial_tab"
    }
}
