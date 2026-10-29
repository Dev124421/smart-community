package com.example.yappy.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.R
import com.example.yappy.model.ChatMessage

/** Chat bubbles: mine on the right (purple), theirs on the left (dark). */
class ChatAdapter(
    private val items: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val row: LinearLayout = view.findViewById(R.id.msg_row)
        val bubble: TextView = view.findViewById(R.id.tv_msg)
        val time: TextView = view.findViewById(R.id.tv_msg_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]
        holder.bubble.text = m.text
        holder.time.text = m.time
        holder.row.gravity = if (m.isMine) Gravity.END else Gravity.START

        val color = if (m.isMine) Color.parseColor("#8B5CF6") else Color.parseColor("#2B2D31")
        ViewCompat.setBackgroundTintList(holder.bubble, ColorStateList.valueOf(color))
    }

    override fun getItemCount() = items.size

    fun add(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }
}