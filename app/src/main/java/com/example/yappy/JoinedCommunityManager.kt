package com.example.yappy

import android.content.Context
import android.content.SharedPreferences
import com.example.yappy.api.ApiCommunity
import com.example.yappy.model.Community
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages persisted joined & created communities scoped strictly per user account.
 * Keeps communities persistent on the left rail while isolating different accounts.
 */
object JoinedCommunityManager {

    private const val PREFS_NAME = "yappy_joined_communities"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Returns a unique storage key for the currently logged-in account */
    fun getAccountKey(context: Context): String {
        val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sessionPrefs.getString("email", null)?.trim()?.lowercase()
        if (!email.isNullOrBlank()) {
            return "joined_" + email.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        }
        val name = sessionPrefs.getString("name", null)?.trim()?.lowercase()
        if (!name.isNullOrBlank()) {
            return "joined_" + name.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        }
        return "joined_active_user"
    }

    @Synchronized
    fun getJoinedCommunities(context: Context): List<Community> {
        val key = getAccountKey(context)
        val prefs = getPrefs(context)
        val jsonString = prefs.getString(key, null)

        if (jsonString.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Community>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Community(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        description = obj.optString("description"),
                        onlineCount = obj.optString("category", "General"),
                        membersCount = obj.optString("membersCount", "1 Member")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun addJoinedCommunity(context: Context, community: Community) {
        val current = getJoinedCommunities(context).toMutableList()
        val index = current.indexOfFirst {
            (community.id.isNotBlank() && it.id == community.id) ||
            it.name.trim().equals(community.name.trim(), ignoreCase = true)
        }
        if (index >= 0) {
            current[index] = community
        } else {
            current.add(community)
        }
        saveJoinedCommunities(context, current)
    }

    @Synchronized
    fun addJoinedCommunity(
        context: Context,
        id: String,
        name: String,
        description: String = "",
        category: String = "General",
        memberCount: String = "1 Member"
    ) {
        addJoinedCommunity(
            context,
            Community(
                id = id,
                name = name,
                description = description,
                onlineCount = category,
                membersCount = memberCount
            )
        )
    }

    /**
     * Synchronizes with backend without deleting communities the active user has joined/created.
     * Updates metadata for known communities, and adds any server communities where isMember is true.
     */
    @Synchronized
    fun syncWithBackend(context: Context, apiList: List<ApiCommunity>): List<Community> {
        val current = getJoinedCommunities(context).toMutableList()
        for (i in current.indices) {
            val local = current[i]
            val remote = apiList.find {
                it.id == local.id || it.name.trim().equals(local.name.trim(), ignoreCase = true)
            }
            if (remote != null) {
                current[i] = local.copy(
                    id = remote.id,
                    name = remote.name,
                    description = remote.description,
                    onlineCount = remote.category,
                    membersCount = "${remote.memberCount} Members"
                )
            }
        }
        for (api in apiList) {
            if (api.isMember) {
                val exists = current.any {
                    it.id == api.id || it.name.trim().equals(api.name.trim(), ignoreCase = true)
                }
                if (!exists) {
                    current.add(
                        Community(
                            id = api.id,
                            name = api.name,
                            description = api.description,
                            onlineCount = api.category,
                            membersCount = "${api.memberCount} Members"
                        )
                    )
                }
            }
        }

        saveJoinedCommunities(context, current)
        return current
    }

    @Synchronized
    fun isJoined(context: Context, id: String?, name: String?): Boolean {
        val list = getJoinedCommunities(context)
        return list.any {
            (!id.isNullOrBlank() && it.id == id) ||
            (!name.isNullOrBlank() && it.name.trim().equals(name.trim(), ignoreCase = true))
        }
    }

    @Synchronized
    fun clearCurrent(context: Context) {
        val key = getAccountKey(context)
        getPrefs(context).edit().remove(key).apply()
        getPrefs(context).edit().remove("joined_list").apply()
    }

    @Synchronized
    private fun saveJoinedCommunities(context: Context, list: List<Community>) {
        try {
            val key = getAccountKey(context)
            val jsonArray = JSONArray()
            for (c in list) {
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("description", c.description)
                    put("category", c.onlineCount)
                    put("membersCount", c.membersCount)
                }
                jsonArray.put(obj)
            }
            getPrefs(context).edit().putString(key, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
