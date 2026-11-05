package com.example.yappy

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import kotlin.math.abs

object CommunityColorHelper {
    private val PALETTE = intArrayOf(
        0xFF5865F2.toInt(),
        0xFFE91E63.toInt(),
        0xFF00B0F4.toInt(),
        0xFF57F287.toInt(),
        0xFFFEE75C.toInt(),
        0xFFED4245.toInt(),
        0xFF9C27B0.toInt(),
        0xFF2ECC71.toInt(),
        0xFFFF9800.toInt(),
        0xFF00BCD4.toInt(),
        0xFF3F51B5.toInt(),
        0xFFE67E22.toInt(),
        0xFF8E44AD.toInt(),
        0xFF1ABC9C.toInt()
    )

    fun getColor(name: String?, id: String?): Int {
        val key = (name.orEmpty() + id.orEmpty()).trim()
        if (key.isEmpty()) return PALETTE[0]
        val hash = abs(key.hashCode())
        return PALETTE[hash % PALETTE.size]
    }

    fun getInitials(name: String?): String {
        if (name.isNullOrBlank()) return "C"
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            words.size >= 2 -> "${words[0].first().uppercase()}${words[1].first().uppercase()}"
            name.length >= 2 -> name.take(2).uppercase()
            else -> name.take(1).uppercase()
        }
    }

    fun applyHighlight(badgeView: View, textView: TextView, name: String?, id: String?) {
        val color = getColor(name, id)
        val initials = getInitials(name)

        textView.text = initials
        if (color == 0xFFFEE75C.toInt()) {
            textView.setTextColor(Color.parseColor("#101010"))
        } else {
            textView.setTextColor(Color.WHITE)
        }

        val density = badgeView.resources.displayMetrics.density
        val cornerRadiusPx = 16f * density

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusPx
            setColor(color)
        }
        badgeView.background = bg
        badgeView.clipToOutline = true
    }
}
