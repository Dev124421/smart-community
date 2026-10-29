package com.example.yappy.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.CommunityColorHelper
import com.example.yappy.R
import com.example.yappy.model.ChatMessage

/**
 * Discord-style chat messages:
 * All messages appear on the left with the sender's avatar, username, and timestamp displayed above the message text.
 */
class ChatAdapter(
    private val items: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val avatarLayout: FrameLayout = view.findViewById(R.id.layout_avatar)
        val avatarInitial: TextView = view.findViewById(R.id.tv_avatar_initial)
        val username: TextView = view.findViewById(R.id.tv_username)
        val time: TextView = view.findViewById(R.id.tv_msg_time)
        val text: TextView = view.findViewById(R.id.tv_msg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]

        holder.username.text = m.senderName

        val userColor = if (m.isMine) {
            Color.parseColor("#5865F2")
        } else {
            CommunityColorHelper.getColor(m.senderName, m.senderName)
        }
        holder.username.setTextColor(userColor)

        val initials = CommunityColorHelper.getInitials(m.senderName)
        holder.avatarInitial.text = initials

        val density = holder.itemView.resources.displayMetrics.density
        val radiusPx = 20f * density
        val avatarBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(userColor)
        }
        holder.avatarLayout.background = avatarBg
        holder.avatarLayout.clipToOutline = true

        holder.time.text = m.time
        holder.text.text = m.text
    }

    override fun getItemCount() = items.size

    fun add(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }
}
