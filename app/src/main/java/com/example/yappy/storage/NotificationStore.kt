package com.example.yappy.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.yappy.model.NotificationItem
import com.example.yappy.model.NotificationType
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object NotificationStore {

    private const val PREFS_NAME = "yappy_notifications"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getAccountKey(context: Context): String {
        val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sessionPrefs.getString("email", null)?.trim()?.lowercase()
        if (!email.isNullOrBlank()) {
            return "notif_" + email.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        }
        val name = sessionPrefs.getString("name", null)?.trim()?.lowercase()
        if (!name.isNullOrBlank()) {
            return "notif_" + name.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        }
        return "notif_active_user"
    }

    @Synchronized
    fun getNotifications(context: Context): MutableList<NotificationItem> {
        val key = getAccountKey(context)
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(key, null)

        if (jsonStr.isNullOrBlank()) {
            ensureInitialUserNotifications(context)
            val updatedJson = prefs.getString(key, null)
            if (updatedJson.isNullOrBlank()) return mutableListOf()
            return parseList(updatedJson)
        }

        val list = parseList(jsonStr)

        // Clean out any legacy demo test entries or verification spam if present
        val cleaned = list.filterNot {
            it.targetName.equals("Alex Johnson", ignoreCase = true) ||
            it.targetName.equals("Sarah Miller", ignoreCase = true) ||
            it.targetName.equals("Night Owls Study Club", ignoreCase = true) ||
            it.type == NotificationType.ACCOUNT_VERIFIED
        }.toMutableList()

        if (cleaned.size != list.size) {
            saveAll(context, cleaned)
        }

        return cleaned
    }

    private fun parseList(jsonStr: String): MutableList<NotificationItem> {
        val list = mutableListOf<NotificationItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.optString("type", NotificationType.SYSTEM.name)
                val type = try {
                    NotificationType.valueOf(typeStr)
                } catch (_: Exception) {
                    NotificationType.SYSTEM
                }

                list.add(
                    NotificationItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        type = type,
                        title = obj.optString("title", "Notification"),
                        message = obj.optString("message", ""),
                        targetName = obj.optString("targetName", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isRead = obj.optBoolean("isRead", false),
                        friendRequestStatus = if (obj.has("friendRequestStatus") && !obj.isNull("friendRequestStatus")) {
                            obj.optString("friendRequestStatus")
                        } else null
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    @Synchronized
    fun ensureInitialUserNotifications(context: Context) {
        val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sessionPrefs.getString("email", null)
        val name = sessionPrefs.getString("name", null)
            ?: email?.substringBefore("@")
            ?: "User"

        val notifPrefs = getPrefs(context)
        val notifFlagKey = "init_notif_added_" + getAccountKey(context)
        val alreadyAdded = notifPrefs.getBoolean(notifFlagKey, false)
        if (alreadyAdded) return

        val now = System.currentTimeMillis()
        val initialList = mutableListOf(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                type = NotificationType.WELCOME,
                title = "Welcome to Yappy",
                message = "Welcome, $name! Explore communities, connect with friends, and start chatting.",
                targetName = name,
                timestamp = now,
                isRead = false
            )
        )

        notifPrefs.edit().putBoolean(notifFlagKey, true).apply()
        saveAll(context, initialList)
    }

    @Synchronized
    fun addServerDeletedNotification(context: Context, communityName: String) {
        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            type = NotificationType.SYSTEM,
            title = "Server Deleted",
            message = "Server '$communityName' was permanently deleted.",
            targetName = communityName,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        addNotification(context, item)
    }

    @Synchronized
    fun addNotification(context: Context, item: NotificationItem) {
        val list = getNotifications(context)
        // Avoid duplicate identical notifications within a short window
        val duplicate = list.any {
            it.type == item.type && it.title == item.title && it.targetName == item.targetName &&
            (System.currentTimeMillis() - it.timestamp) < 5000
        }
        if (!duplicate) {
            list.add(0, item)
            saveAll(context, list)
        }
    }

    @Synchronized
    fun addCommunityCreatedNotification(context: Context, communityName: String) {
        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            type = NotificationType.COMMUNITY_CREATED,
            title = "Community Created",
            message = "You successfully created '$communityName'. Welcome your members!",
            targetName = communityName,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        addNotification(context, item)
    }

    @Synchronized
    fun addJoinedNotification(context: Context, communityName: String) {
        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            type = NotificationType.JOINED_COMMUNITY,
            title = "Joined Community",
            message = "You joined '$communityName'. Say hello in #general!",
            targetName = communityName,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        addNotification(context, item)
    }

    @Synchronized
    fun addLeftNotification(context: Context, communityName: String) {
        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            type = NotificationType.LEFT_COMMUNITY,
            title = "Left Community",
            message = "You left '$communityName'.",
            targetName = communityName,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        addNotification(context, item)
    }

    @Synchronized
    fun addFriendAcceptedNotification(context: Context, friendName: String) {
        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            type = NotificationType.FRIEND_REQUEST,
            title = "Friend Request Accepted",
            message = "You and $friendName are now connected! You can start chatting in direct messages.",
            targetName = friendName,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            friendRequestStatus = "ACCEPTED"
        )
        addNotification(context, item)
    }

    @Synchronized
    fun addFriendRequestNotification(context: Context, friendName: String) {
        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            type = NotificationType.FRIEND_REQUEST,
            title = "Friend Request",
            message = "$friendName sent you a friend request. Connect to chat in direct messages!",
            targetName = friendName,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            friendRequestStatus = "PENDING"
        )
        addNotification(context, item)
    }

    @Synchronized
    fun updateFriendRequestStatus(context: Context, notificationId: String, status: String) {
        val list = getNotifications(context)
        val index = list.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            val current = list[index]
            val updatedTitle = if (status == "ACCEPTED") "Friend Request Accepted" else "Friend Request Declined"
            val updatedMessage = if (status == "ACCEPTED") {
                "You and ${current.targetName} are now connected! You can start chatting in direct messages."
            } else {
                "You declined the friend request from ${current.targetName}."
            }
            list[index] = current.copy(
                title = updatedTitle,
                friendRequestStatus = status,
                isRead = true,
                message = updatedMessage
            )
            saveAll(context, list)
        }
    }

    @Synchronized
    fun markAllAsRead(context: Context) {
        val list = getNotifications(context)
        val updated = list.map { it.copy(isRead = true) }
        saveAll(context, updated)
    }

    @Synchronized
    fun clearAll(context: Context) {
        saveAll(context, emptyList())
    }

    @Synchronized
    fun getUnreadCount(context: Context): Int {
        return getNotifications(context).count { !it.isRead }
    }

    @Synchronized
    private fun saveAll(context: Context, list: List<NotificationItem>) {
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("type", item.type.name)
                put("title", item.title)
                put("message", item.message)
                put("targetName", item.targetName)
                put("timestamp", item.timestamp)
                put("isRead", item.isRead)
                put("friendRequestStatus", item.friendRequestStatus)
            }
            arr.put(obj)
        }
        val key = getAccountKey(context)
        getPrefs(context).edit().putString(key, arr.toString()).apply()
    }
}
