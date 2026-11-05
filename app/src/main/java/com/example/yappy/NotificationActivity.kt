package com.example.yappy

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.adapter.NotificationAdapter
import com.example.yappy.model.NotificationItem
import com.example.yappy.model.NotificationType
import com.example.yappy.storage.NotificationStore

class NotificationActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var tvUnreadCount: TextView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var btnMarkAllRead: TextView

    private lateinit var tabAll: TextView
    private lateinit var tabCommunities: TextView
    private lateinit var tabFriends: TextView

    private var currentFilter: String = "ALL"
    private var allNotifications: MutableList<NotificationItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        setupWindowInsets()
        bindViews()
        setupRecyclerView()
        setupListeners()
        loadNotifications()
        NotificationStore.markAllAsRead(this)
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun setupWindowInsets() {
        val minTop = (15 * resources.displayMetrics.density).toInt()
        val minBottom = (25 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notifications_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                bars.left,
                maxOf(bars.top, minTop),
                bars.right,
                maxOf(bars.bottom, minBottom)
            )
            insets
        }
    }

    private fun bindViews() {
        rvNotifications = findViewById(R.id.rv_notifications)
        tvUnreadCount = findViewById(R.id.tv_unread_count)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read)

        tabAll = findViewById(R.id.tab_all)
        tabCommunities = findViewById(R.id.tab_communities)
        tabFriends = findViewById(R.id.tab_friends)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(mutableListOf()) { item, isAccept ->
            handleFriendRequestAction(item, isAccept)
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun setupListeners() {
        tabAll.setOnClickListener {
            switchFilter("ALL")
        }

        tabCommunities.setOnClickListener {
            switchFilter("COMMUNITIES")
        }

        tabFriends.setOnClickListener {
            switchFilter("FRIENDS")
        }

        btnMarkAllRead.setOnClickListener {
            NotificationStore.markAllAsRead(this)
            Toast.makeText(this, "Marked all as read", Toast.LENGTH_SHORT).show()
            loadNotifications()
        }
    }

    private fun loadNotifications() {
        allNotifications = NotificationStore.getNotifications(this)
        val unreadCount = allNotifications.count { !it.isRead }
        if (unreadCount > 0) {
            tvUnreadCount.visibility = View.VISIBLE
            tvUnreadCount.text = "$unreadCount new"
        } else {
            tvUnreadCount.visibility = View.GONE
        }
        applyCurrentFilter()
    }

    private fun switchFilter(filter: String) {
        currentFilter = filter

        val activeBg = R.drawable.bg_tab_chip_active
        val inactiveBg = R.drawable.bg_tab_chip_inactive
        val activeTextColor = Color.parseColor("#101010")
        val inactiveTextColor = Color.parseColor("#E4E2DE")

        tabAll.setBackgroundResource(if (filter == "ALL") activeBg else inactiveBg)
        tabAll.setTextColor(if (filter == "ALL") activeTextColor else inactiveTextColor)

        tabCommunities.setBackgroundResource(if (filter == "COMMUNITIES") activeBg else inactiveBg)
        tabCommunities.setTextColor(if (filter == "COMMUNITIES") activeTextColor else inactiveTextColor)

        tabFriends.setBackgroundResource(if (filter == "FRIENDS") activeBg else inactiveBg)
        tabFriends.setTextColor(if (filter == "FRIENDS") activeTextColor else inactiveTextColor)

        applyCurrentFilter()
    }

    private fun applyCurrentFilter() {
        val filtered = when (currentFilter) {
            "COMMUNITIES" -> allNotifications.filter {
                it.type == NotificationType.COMMUNITY_CREATED ||
                it.type == NotificationType.JOINED_COMMUNITY ||
                it.type == NotificationType.LEFT_COMMUNITY
            }
            "FRIENDS" -> allNotifications.filter {
                it.type == NotificationType.FRIEND_REQUEST
            }
            else -> allNotifications
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
        }
    }

    private fun handleFriendRequestAction(item: NotificationItem, isAccept: Boolean) {
        val newStatus = if (isAccept) "ACCEPTED" else "DECLINED"
        NotificationStore.updateFriendRequestStatus(this, item.id, newStatus)

        if (isAccept) {
            Toast.makeText(this, "Accepted friend request from ${item.targetName}!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Declined friend request from ${item.targetName}", Toast.LENGTH_SHORT).show()
        }

        loadNotifications()
    }
}
