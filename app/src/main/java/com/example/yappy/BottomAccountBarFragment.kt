package com.example.yappy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.yappy.storage.NotificationStore

class BottomAccountBarFragment : Fragment() {

    private var tvUsername: TextView? = null
    private var badgeNotificationDot: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_account_bar, container, false)

        tvUsername = view.findViewById(R.id.tv_username)
        badgeNotificationDot = view.findViewById(R.id.badge_notification_dot)

        val openAccount = {
            startActivity(Intent(requireContext(), Account::class.java))
        }

        // Clicking anywhere on the account bar, avatar or username redirects to Account
        view.findViewById<View>(R.id.bottom_account_bar_root)?.setOnClickListener { openAccount() }
        view.findViewById<View>(R.id.avatar_container)?.setOnClickListener { openAccount() }
        view.findViewById<View>(R.id.user_info_layout)?.setOnClickListener { openAccount() }

        // Clicking the notification icon or its frame redirects to Notifications
        val openNotifications = {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
        view.findViewById<View>(R.id.layout_btn_notification)?.setOnClickListener { openNotifications() }
        view.findViewById<View>(R.id.btn_notification)?.setOnClickListener { openNotifications() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUserInfo()
        updateNotificationDot()
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
        updateNotificationDot()
    }

    fun updateUserInfo() {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val profiles = ctx.getSharedPreferences("user_profiles", Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        var name = prefs.getString("name", null)
        if (name.isNullOrBlank() && !email.isNullOrBlank()) {
            name = profiles.getString(email, null)
        }
        val displayName = when {
            !name.isNullOrBlank() -> name
            !email.isNullOrBlank() -> email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            else -> "User"
        }
        tvUsername?.text = displayName
    }

    fun updateNotificationDot() {
        val ctx = context ?: return
        val hasUnread = NotificationStore.getUnreadCount(ctx) > 0
        badgeNotificationDot?.visibility = if (hasUnread) View.VISIBLE else View.GONE
    }
}
