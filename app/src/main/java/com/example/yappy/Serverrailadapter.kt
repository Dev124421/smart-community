package com.example.yappy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.R
import com.example.yappy.model.Community

/** Small round icons in the left rail of the home screen (one per joined community). */
class ServerRailAdapter(
    private var items: List<Community>,
    private val onClick: (Community) -> Unit
) : RecyclerView.Adapter<ServerRailAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val initial: TextView = view.findViewById(R.id.tv_rail_initial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rail_server, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val community = items[position]
        holder.initial.text = community.name.trim().take(1).uppercase()
        holder.itemView.setOnClickListener { onClick(community) }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Community>) {
        items = list
        notifyDataSetChanged()
    }
}