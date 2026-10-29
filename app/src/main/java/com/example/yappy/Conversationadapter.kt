package com.example.yappy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.R
import com.example.yappy.model.Conversation

/** Direct-message list shown on the personalMessage screen. */
class ConversationAdapter(
    private val items: MutableList<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val initial: TextView = view.findViewById(R.id.tv_conv_initial)
        val name: TextView = view.findViewById(R.id.tv_conv_name)
        val last: TextView = view.findViewById(R.id.tv_conv_last)
        val time: TextView = view.findViewById(R.id.tv_conv_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        val firstLetter = c.name.trim().take(1).uppercase()
        holder.initial.text = if (firstLetter.isNotBlank()) firstLetter else "?"
        holder.initial.setBackgroundResource(R.drawable.bg_avatar_green)
        holder.initial.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        holder.name.text = c.name
        holder.last.text = c.lastMessage
        holder.time.text = c.time
        holder.itemView.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = items.size

    fun addFirst(conversation: Conversation) {
        items.add(0, conversation)
        notifyItemInserted(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Conversation>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}
