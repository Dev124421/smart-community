package com.example.yappy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.yappy.CommunityColorHelper
import com.example.yappy.R
import com.example.yappy.model.Community

/** Small squircle icons in the left rail representing joined/created communities with vibrant colors. */
class ServerRailAdapter(
    private var items: List<Community>,
    private var selectedId: String? = null,
    private val onClick: (Community) -> Unit
) : RecyclerView.Adapter<ServerRailAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val badgeLayout: FrameLayout = view.findViewById(R.id.layout_rail_badge)
        val initial: TextView = view.findViewById(R.id.tv_rail_initial)
        val indicator: View? = view.findViewById(R.id.indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rail_server, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val community = items[position]
        CommunityColorHelper.applyHighlight(
            holder.badgeLayout,
            holder.initial,
            community.name,
            community.id
        )
        val isSelected = selectedId != null && (
            selectedId == community.id ||
            selectedId.equals(community.name, ignoreCase = true)
        )
        holder.indicator?.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.badgeLayout.setOnClickListener {
            onClick(community)
        }
        holder.badgeLayout.setOnLongClickListener {
            Toast.makeText(holder.itemView.context, community.name, Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Community>, selected: String? = null) {
        items = list
        selectedId = selected
        notifyDataSetChanged()
    }
}
