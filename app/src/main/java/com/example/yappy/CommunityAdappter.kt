package com.example.yappy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.R
import com.example.yappy.model.Community
import com.google.android.material.button.MaterialButton

class CommunityAdapter(
    private val items: MutableList<Community>,
    private val onPreviewClick: (Community) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBanner: ImageView = itemView.findViewById(R.id.iv_banner)
        val ivCommunityIcon: TextView = itemView.findViewById(R.id.iv_community_icon)
        val tvCommunityName: TextView = itemView.findViewById(R.id.tv_community_name)
        val tvCommunityDesc: TextView = itemView.findViewById(R.id.tv_community_desc)
        val tvOnlineCount: TextView = itemView.findViewById(R.id.tv_online_count)
        val tvMembersCount: TextView = itemView.findViewById(R.id.tv_members_count)
        val btnPreview: MaterialButton = itemView.findViewById(R.id.btn_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server_community, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = items[position]

        holder.tvCommunityName.text = community.name
        holder.tvCommunityDesc.text = community.description
        holder.tvOnlineCount.text = community.onlineCount
        holder.tvMembersCount.text = community.membersCount
        holder.ivCommunityIcon.text = community.name.firstOrNull()?.uppercase() ?: "?"

        community.bannerRes?.let { holder.ivBanner.setImageResource(it) }

        holder.btnPreview.setOnClickListener { onPreviewClick(community) }
        holder.itemView.setOnClickListener { onPreviewClick(community) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<Community>) {
        val oldItems = items.toList()
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldItems.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(o: Int, n: Int) = oldItems[o].id == newItems[n].id
            override fun areContentsTheSame(o: Int, n: Int) = oldItems[o] == newItems[n]
        })
        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }
}