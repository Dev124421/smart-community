package com.example.yappy.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.CommunityColorHelper
import com.example.yappy.R
import com.example.yappy.model.ServerMember

class MemberAdapter(
    private val onMemberClick: (ServerMember) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    sealed class ListItem {
        data class Header(val title: String, val isOnline: Boolean) : ListItem()
        data class Member(val member: ServerMember) : ListItem()
    }

    private var allMembers: List<ServerMember> = emptyList()
    private val displayItems: MutableList<ListItem> = mutableListOf()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MEMBER = 1
    }

    fun submitList(members: List<ServerMember>, query: String = "") {
        allMembers = members
        filter(query)
    }

    fun filter(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isBlank()) {
            allMembers
        } else {
            allMembers.filter {
                it.username.lowercase().contains(q) ||
                it.id.lowercase().contains(q) ||
                it.handle.lowercase().contains(q) ||
                it.email.lowercase().contains(q)
            }
        }

        val online = filtered.filter { it.isOnline }
        val offline = filtered.filter { !it.isOnline }

        displayItems.clear()
        if (online.isNotEmpty()) {
            displayItems.add(ListItem.Header("ONLINE — ${online.size}", true))
            online.forEach { displayItems.add(ListItem.Member(it)) }
        }
        if (offline.isNotEmpty()) {
            displayItems.add(ListItem.Header("OFFLINE — ${offline.size}", false))
            offline.forEach { displayItems.add(ListItem.Member(it)) }
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Member -> TYPE_MEMBER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val v = inflater.inflate(R.layout.item_member_section_header, parent, false)
            HeaderViewHolder(v)
        } else {
            val v = inflater.inflate(R.layout.item_server_member, parent, false)
            MemberViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.Member -> (holder as MemberViewHolder).bind(item.member, onMemberClick)
        }
    }

    override fun getItemCount(): Int = displayItems.size

    fun isEmpty(): Boolean = displayItems.isEmpty()

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_header_title)
        private val viewDot: View = itemView.findViewById(R.id.view_status_dot)

        fun bind(header: ListItem.Header) {
            tvTitle.text = header.title
            if (header.isOnline) {
                viewDot.setBackgroundResource(R.drawable.online)
            } else {
                viewDot.setBackgroundResource(R.drawable.bg_status_offline)
            }
        }
    }

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutAvatar: FrameLayout = itemView.findViewById(R.id.layout_member_avatar)
        private val tvAvatarInitial: TextView = itemView.findViewById(R.id.tv_member_avatar_initial)
        private val ivPresenceDot: ImageView = itemView.findViewById(R.id.iv_presence_dot)
        private val tvName: TextView = itemView.findViewById(R.id.tv_member_name)
        private val tvId: TextView = itemView.findViewById(R.id.tv_member_id)
        private val tvRole: TextView = itemView.findViewById(R.id.tv_member_role)

        fun bind(member: ServerMember, onClick: (ServerMember) -> Unit) {
            tvName.text = member.username
            tvId.text = "${member.handle} • ID: #${member.id}"
            tvAvatarInitial.text = CommunityColorHelper.getInitials(member.username)

            val color = CommunityColorHelper.getColor(member.username, member.username)
            val density = itemView.resources.displayMetrics.density
            val radiusPx = 19f * density
            val avatarBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radiusPx
                setColor(color)
            }
            layoutAvatar.background = avatarBg

            if (member.isOnline) {
                ivPresenceDot.setImageResource(R.drawable.bg_status_online)
            } else {
                ivPresenceDot.setImageResource(R.drawable.bg_status_offline)
            }

            when (member.role.uppercase()) {
                "OWNER" -> {
                    tvRole.visibility = View.VISIBLE
                    tvRole.text = "👑 Owner"
                    tvRole.setTextColor(Color.parseColor("#FF8FA3"))
                }
                "ADMIN" -> {
                    tvRole.visibility = View.VISIBLE
                    tvRole.text = "Admin"
                    tvRole.setTextColor(Color.parseColor("#B794F6"))
                }
                "BOT" -> {
                    tvRole.visibility = View.VISIBLE
                    tvRole.text = "BOT"
                    tvRole.setTextColor(Color.parseColor("#80D8B0"))
                }
                else -> {
                    tvRole.visibility = View.VISIBLE
                    tvRole.text = "Member"
                    tvRole.setTextColor(Color.parseColor("#E4E2DE"))
                }
            }

            itemView.setOnClickListener { onClick(member) }
        }
    }
}
