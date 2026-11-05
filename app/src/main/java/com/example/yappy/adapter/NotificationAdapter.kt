package com.example.yappy.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.R
import com.example.yappy.model.NotificationItem
import com.example.yappy.model.NotificationType
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private val items: MutableList<NotificationItem>,
    private val onActionClick: (NotificationItem, Boolean) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconFrame: FrameLayout = view.findViewById(R.id.icon_frame)
        val ivType: ImageView = view.findViewById(R.id.iv_notif_type)
        val tvTitle: TextView = view.findViewById(R.id.tv_notif_title)
        val dotUnread: View = view.findViewById(R.id.dot_unread)
        val tvTime: TextView = view.findViewById(R.id.tv_notif_time)
        val tvMessage: TextView = view.findViewById(R.id.tv_notif_message)
        val layoutFriendActions: LinearLayout = view.findViewById(R.id.layout_friend_actions)
        val btnAccept: Button = view.findViewById(R.id.btn_accept_friend)
        val btnDecline: Button = view.findViewById(R.id.btn_decline_friend)
        val tvFriendStatus: TextView = view.findViewById(R.id.tv_friend_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message
        holder.tvTime.text = formatRelativeTime(item.timestamp)
        holder.dotUnread.visibility = if (!item.isRead) View.VISIBLE else View.GONE

        when (item.type) {
            NotificationType.ACCOUNT_VERIFIED -> {
                holder.ivType.setImageResource(R.drawable.verified)
                holder.ivType.imageTintList = ColorStateList.valueOf(Color.parseColor("#80D8B0")) // Green
                holder.layoutFriendActions.visibility = View.GONE
                holder.tvFriendStatus.visibility = View.GONE
            }
            NotificationType.WELCOME -> {
                holder.ivType.setImageResource(R.drawable.logo)
                holder.ivType.imageTintList = null // Keep original logo colors or tint
                holder.layoutFriendActions.visibility = View.GONE
                holder.tvFriendStatus.visibility = View.GONE
            }
            NotificationType.COMMUNITY_CREATED -> {
                holder.ivType.setImageResource(R.drawable.ic_community)
                holder.ivType.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFD166")) // Yellow
                holder.layoutFriendActions.visibility = View.GONE
                holder.tvFriendStatus.visibility = View.GONE
            }
            NotificationType.JOINED_COMMUNITY -> {
                holder.ivType.setImageResource(R.drawable.verified)
                holder.ivType.imageTintList = ColorStateList.valueOf(Color.parseColor("#80D8B0")) // Green
                holder.layoutFriendActions.visibility = View.GONE
                holder.tvFriendStatus.visibility = View.GONE
            }
            NotificationType.LEFT_COMMUNITY -> {
                holder.ivType.setImageResource(R.drawable.ic_logout)
                holder.ivType.imageTintList = ColorStateList.valueOf(Color.parseColor("#ED4245")) // Red
                holder.layoutFriendActions.visibility = View.GONE
                holder.tvFriendStatus.visibility = View.GONE
            }
            NotificationType.FRIEND_REQUEST -> {
                holder.ivType.setImageResource(R.drawable.ic_user)
                holder.ivType.imageTintList = ColorStateList.valueOf(Color.parseColor("#B794F6")) // Purple

                when (item.friendRequestStatus) {
                    "PENDING" -> {
                        holder.layoutFriendActions.visibility = View.VISIBLE
                        holder.tvFriendStatus.visibility = View.GONE

                        holder.btnAccept.setOnClickListener {
                            onActionClick(item, true)
                        }
                        holder.btnDecline.setOnClickListener {
                            onActionClick(item, false)
                        }
                    }
                    "ACCEPTED" -> {
                        holder.layoutFriendActions.visibility = View.GONE
                        holder.tvFriendStatus.visibility = View.VISIBLE
                        holder.tvFriendStatus.text = "✓ Friend Request Accepted"
                        holder.tvFriendStatus.setTextColor(Color.parseColor("#80D8B0"))
                    }
                    "DECLINED" -> {
                        holder.layoutFriendActions.visibility = View.GONE
                        holder.tvFriendStatus.visibility = View.VISIBLE
                        holder.tvFriendStatus.text = "Friend Request Declined"
                        holder.tvFriendStatus.setTextColor(Color.parseColor("#949BA4"))
                    }
                    else -> {
                        holder.layoutFriendActions.visibility = View.GONE
                        holder.tvFriendStatus.visibility = View.GONE
                    }
                }
            }
            NotificationType.SYSTEM -> {
                holder.ivType.setImageResource(R.drawable.notification)
                holder.ivType.imageTintList = ColorStateList.valueOf(Color.parseColor("#B794F6")) // Purple
                holder.layoutFriendActions.visibility = View.GONE
                holder.tvFriendStatus.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<NotificationItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        if (diff < 60_000) return "Just now"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (minutes < 60) return "${minutes}m ago"
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if (hours < 24) return "${hours}h ago"
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        if (days == 1L) return "Yesterday"
        return "${days}d ago"
    }
}
