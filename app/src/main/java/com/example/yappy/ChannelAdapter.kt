package com.example.yappy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.R
import com.example.yappy.api.Channel

class ChannelAdapter(
    private var items: List<Channel>,
    private var activeChannelId: String? = null,
    private val onChannelClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view.findViewById(R.id.channel_item_root)
        val icon: ImageView = view.findViewById(R.id.iv_channel_icon)
        val name: TextView = view.findViewById(R.id.tv_channel_name)
        val unreadDot: View = view.findViewById(R.id.unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val channel = items[position]
        holder.name.text = channel.name

        val isVoice = channel.type.equals("voice", ignoreCase = true)
        val isSelected = activeChannelId != null && (
            activeChannelId == channel.id ||
            activeChannelId.equals(channel.name, ignoreCase = true)
        )

        if (isVoice) {
            holder.icon.setImageResource(R.drawable.ic_speaker)
        } else {
            holder.icon.setImageResource(R.drawable.ic_hashtag)
        }

        if (isSelected) {
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.warm_white))
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.warm_white))
            holder.root.setBackgroundResource(R.drawable.bg_template_card)
        } else {
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.soft_border))
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.soft_border))
            holder.root.background = null
        }

        holder.root.setOnClickListener {
            activeChannelId = channel.id
            notifyDataSetChanged()
            onChannelClick(channel)
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submit(newItems: List<Channel>, selectedId: String? = null) {
        items = newItems
        if (selectedId != null) activeChannelId = selectedId
        notifyDataSetChanged()
    }
}
