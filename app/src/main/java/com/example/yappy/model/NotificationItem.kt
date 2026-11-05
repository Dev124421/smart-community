package com.example.yappy.model

enum class NotificationType {
    ACCOUNT_VERIFIED,
    WELCOME,
    COMMUNITY_CREATED,
    JOINED_COMMUNITY,
    LEFT_COMMUNITY,
    FRIEND_REQUEST,
    SYSTEM
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val targetName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val friendRequestStatus: String? = null // "PENDING", "ACCEPTED", "DECLINED"
)
