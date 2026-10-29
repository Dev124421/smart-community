package com.example.yappy

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import kotlin.math.abs

/**
 * Helper to dynamically assign distinctive, vibrant Discord/Yappy-style colors and initials
 * to represent communities on the left rail.
 */
object CommunityColorHelper {
    private val PALETTE = intArrayOf(
        0xFF5865F2.toInt(), // Discord Blurple / Royal Blue
        0xFFE91E63.toInt(), // Vibrant Rose / Neon Pink
        0xFF00B0F4.toInt(), // Cyan / Electric Blue
        0xFF57F287.toInt(), // Emerald Green
        0xFFFEE75C.toInt(), // Amber Gold
        0xFFED4245.toInt(), // Coral Red
        0xFF9C27B0.toInt(), // Deep Purple
        0xFF2ECC71.toInt(), // Mint Green
        0xFFFF9800.toInt(), // Bright Orange
        0xFF00BCD4.toInt(), // Teal
        0xFF3F51B5.toInt(), // Indigo
        0xFFE67E22.toInt(), // Warm Orange
        0xFF8E44AD.toInt(), // Amethyst Violet
        0xFF1ABC9C.toInt()  // Turquoise
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

    /**
     * Highlights the community badge with a distinct, vibrant rounded squircle background
     * and high-contrast initial text representing the community.
     */
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
        val cornerRadiusPx = 16f * density // 16dp rounded squircle like Discord servers

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusPx
            setColor(color)
        }
        badgeView.background = bg
        badgeView.clipToOutline = true
    }
}
