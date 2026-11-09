package com.example.yappy

import android.content.Context
import android.content.SharedPreferences
import com.example.yappy.api.ApiCommunity
import com.example.yappy.model.Community
import org.json.JSONArray
import org.json.JSONObject

object JoinedCommunityManager {

    private const val PREFS_NAME = "yappy_joined_communities"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

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
        // Register in discoverable pool so if left later it returns to discoverable list
        addDiscoverableCommunity(context, community)
    }

    @Synchronized
    fun addJoinedCommunity(
        context: Context,
        id: String,
        name: String,
        description: String = "",
        category: String = "General",
        memberCount: String = "1 Member",
        isOwner: Boolean = false
    ) {
        if (isOwner || id.startsWith("local_")) {
            setServerOwner(context, id, name)
        }
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

    @Synchronized
    fun setServerOwner(context: Context, communityId: String, communityName: String) {
        val prefs = getPrefs(context)
        val key = getAccountKey(context)
        val ownedSet = prefs.getStringSet("owned_servers_$key", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (communityId.isNotBlank()) ownedSet.add(communityId)
        if (communityName.isNotBlank()) ownedSet.add(communityName.trim().lowercase())
        prefs.edit().putStringSet("owned_servers_$key", ownedSet).apply()
    }

    @Synchronized
    fun isOwner(context: Context, communityId: String, communityName: String): Boolean {
        val prefs = getPrefs(context)
        val key = getAccountKey(context)
        val ownedSet = prefs.getStringSet("owned_servers_$key", emptySet()) ?: emptySet()
        if (communityId.isNotBlank() && ownedSet.contains(communityId)) return true
        if (communityName.isNotBlank() && ownedSet.contains(communityName.trim().lowercase())) return true
        return false
    }

    @Synchronized
    fun removeServerOwner(context: Context, communityId: String, communityName: String) {
        val prefs = getPrefs(context)
        val key = getAccountKey(context)
        val ownedSet = prefs.getStringSet("owned_servers_$key", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (communityId.isNotBlank()) ownedSet.remove(communityId)
        if (communityName.isNotBlank()) ownedSet.remove(communityName.trim().lowercase())
        prefs.edit().putStringSet("owned_servers_$key", ownedSet).apply()
    }

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
            if (api.isMember || isOwner(context, api.id, api.name)) {
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

    private const val PREFS_DISCOVERABLE_KEY = "discoverable_pool"

    fun getDefaultDiscoverable(): List<Community> = listOf(
        Community("3", "Study Lounge", "Collaborate on homework and projects", "Education", "12K Members"),
        Community("4", "Anime & Manga", "Discuss latest episodes and releases", "Entertainment", "88K Members"),
        Community("5", "Dev Hangout", "Kotlin, Android & Full Stack discussions", "Tech", "34K Members")
    )

    @Synchronized
    fun getDiscoverableCommunities(context: Context): List<Community> {
        val prefs = getPrefs(context)
        val jsonString = prefs.getString(PREFS_DISCOVERABLE_KEY, null)
        val list = mutableListOf<Community>()

        // 1. Default initial communities
        list.addAll(getDefaultDiscoverable())

        // 2. Discoverable communities saved locally (including left communities)
        if (!jsonString.isNullOrBlank()) {
            try {
                val arr = JSONArray(jsonString)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val comm = Community(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        description = obj.optString("description"),
                        onlineCount = obj.optString("category", "General"),
                        membersCount = obj.optString("membersCount", "Explore Community")
                    )
                    val exists = list.any {
                        (comm.id.isNotBlank() && it.id == comm.id) ||
                        it.name.trim().equals(comm.name.trim(), ignoreCase = true)
                    }
                    if (!exists) {
                        list.add(comm)
                    }
                }
            } catch (_: Exception) {
            }
        }
        return list
    }

    @Synchronized
    fun addDiscoverableCommunity(context: Context, community: Community) {
        val current = getDiscoverableCommunities(context).toMutableList()
        val index = current.indexOfFirst {
            (community.id.isNotBlank() && it.id == community.id) ||
            it.name.trim().equals(community.name.trim(), ignoreCase = true)
        }
        if (index >= 0) {
            current[index] = community
        } else {
            current.add(community)
        }
        saveDiscoverableCommunities(context, current)
    }

    @Synchronized
    fun removeDiscoverableCommunity(context: Context, id: String?, name: String?) {
        val current = getDiscoverableCommunities(context).toMutableList()
        val removed = current.removeAll {
            (!id.isNullOrBlank() && it.id == id) ||
            (!name.isNullOrBlank() && it.name.trim().equals(name.trim(), ignoreCase = true))
        }
        if (removed) {
            saveDiscoverableCommunities(context, current)
        }
    }

    @Synchronized
    private fun saveDiscoverableCommunities(context: Context, list: List<Community>) {
        try {
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
            getPrefs(context).edit().putString(PREFS_DISCOVERABLE_KEY, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun removeJoinedCommunity(context: Context, id: String?, name: String?): Boolean {
        val current = getJoinedCommunities(context).toMutableList()
        val target = current.find {
            (!id.isNullOrBlank() && it.id == id) ||
            (!name.isNullOrBlank() && it.name.trim().equals(name.trim(), ignoreCase = true))
        }
        val removed = current.removeAll {
            (!id.isNullOrBlank() && it.id == id) ||
            (!name.isNullOrBlank() && it.name.trim().equals(name.trim(), ignoreCase = true))
        }
        if (removed) {
            removeServerOwner(context, id ?: "", name ?: "")
            saveJoinedCommunities(context, current)

            // Return the left community to the discoverable pool so it appears in rv_joined_communities on homescreen
            if (target != null) {
                addDiscoverableCommunity(context, target.copy(membersCount = "Explore Community"))
            }

            val communityName = name ?: "Community"
            com.example.yappy.storage.NotificationStore.addLeftNotification(context, communityName)
        }
        return removed
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
