package com.example.yappy.storage

import android.content.Context
import com.example.yappy.JoinedCommunityManager
import com.example.yappy.model.ServerMember
import org.json.JSONArray
import org.json.JSONObject

object ServerMemberStore {

    private const val PREFS_NAME = "yappy_server_members"

    /**
     * Retrieves the list of REAL members for the specified community.
     * Only displays members who have actually joined this community or have it in their rail.
     * Zero fake accounts.
     */
    @Synchronized
    fun getMembers(context: Context, communityId: String, communityName: String): List<ServerMember> {
        val memberMap = mutableMapOf<String, ServerMember>()

        // 1. Current logged-in user
        val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val currentName = sessionPrefs.getString("name", null)
            ?: sessionPrefs.getString("email", null)?.substringBefore("@")
            ?: "You"
        val currentEmail = sessionPrefs.getString("email", "") ?: ""
        val currentUserId = sessionPrefs.getString("user_id", "usr_self") ?: "usr_self"

        // Check if current user has this community in their item_rail / joined list
        val joinedCommunities = JoinedCommunityManager.getJoinedCommunities(context)
        val isJoinedByCurrentUser = joinedCommunities.any {
            (communityId.isNotBlank() && it.id == communityId) ||
            (communityName.isNotBlank() && it.name.trim().equals(communityName.trim(), ignoreCase = true))
        }

        val isOwner = JoinedCommunityManager.isOwner(context, communityId, communityName)
        if (isJoinedByCurrentUser || isOwner) {
            val role = if (isOwner) "OWNER" else "MEMBER"
            val currentUserMember = ServerMember(
                id = currentUserId,
                username = currentName,
                email = currentEmail,
                handle = "@${currentName.lowercase().replace(" ", "").replace("[^a-z0-9_]".toRegex(), "")}",
                role = role,
                isOnline = true,
                customStatus = "Active now"
            )
            memberMap[currentName.lowercase()] = currentUserMember
        }

        // 2. Load previously remembered real members for this community from storage
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = normalizeKey(communityId.ifBlank { communityName })
        val jsonString = prefs.getString(key, null)

        if (!jsonString.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val username = obj.optString("username", "")
                    if (username.isNotBlank()) {
                        val isSelf = username.equals(currentName, ignoreCase = true)
                        val m = ServerMember(
                            id = obj.optString("id", "usr_${username.lowercase()}"),
                            username = username,
                            email = obj.optString("email", ""),
                            handle = obj.optString("handle", "@${username.lowercase()}"),
                            role = if (isSelf) (memberMap[currentName.lowercase()]?.role ?: "MEMBER") else obj.optString("role", "MEMBER"),
                            isOnline = if (isSelf) true else obj.optBoolean("isOnline", false),
                            customStatus = if (obj.has("customStatus") && !obj.isNull("customStatus")) obj.getString("customStatus") else null,
                            joinedAt = obj.optString("joinedAt", "Joined server")
                        )
                        if (!isSelf || !memberMap.containsKey(currentName.lowercase())) {
                            memberMap[username.lowercase()] = m
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        // 3. Check other accounts on this device that have this community joined in their rail
        val joinedPrefs = context.getSharedPreferences("yappy_joined_communities", Context.MODE_PRIVATE)
        val profilesPrefs = context.getSharedPreferences("user_profiles", Context.MODE_PRIVATE)
        val allJoinedEntries = joinedPrefs.all

        for ((accountKey, value) in allJoinedEntries) {
            if (accountKey.startsWith("joined_") && value is String && value.isNotBlank()) {
                try {
                    val arr = JSONArray(value)
                    var hasCommunity = false
                    for (i in 0 until arr.length()) {
                        val cObj = arr.getJSONObject(i)
                        val cId = cObj.optString("id")
                        val cName = cObj.optString("name")
                        if ((communityId.isNotBlank() && cId == communityId) ||
                            (communityName.isNotBlank() && cName.equals(communityName, ignoreCase = true))) {
                            hasCommunity = true
                            break
                        }
                    }

                    if (hasCommunity) {
                        val rawEmailOrName = accountKey.removePrefix("joined_").replace("_", "@")
                        val knownName = profilesPrefs.getString(rawEmailOrName, null)
                            ?: rawEmailOrName.substringBefore("@")
                        val cleanName = knownName.ifBlank { rawEmailOrName }

                        if (!memberMap.containsKey(cleanName.lowercase())) {
                            memberMap[cleanName.lowercase()] = ServerMember(
                                id = "usr_${cleanName.lowercase().replace("[^a-z0-9]".toRegex(), "")}",
                                username = cleanName,
                                email = if (rawEmailOrName.contains("@")) rawEmailOrName else "",
                                handle = "@${cleanName.lowercase().replace(" ", "").replace("[^a-z0-9_]".toRegex(), "")}",
                                role = "MEMBER",
                                isOnline = false,
                                customStatus = "Offline",
                                joinedAt = "Joined server"
                            )
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }

        return memberMap.values.toList()
    }

    /**
     * Merges real members returned from the backend API into the local member store.
     */
    @Synchronized
    fun mergeApiMembers(context: Context, communityId: String, apiMembers: List<ServerMember>) {
        if (apiMembers.isEmpty()) return

        val current = getMembers(context, communityId, "").toMutableList()
        val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val currentName = sessionPrefs.getString("name", null)
            ?: sessionPrefs.getString("email", null)?.substringBefore("@")
            ?: "You"
        val currentEmail = sessionPrefs.getString("email", "") ?: ""

        for (apiMember in apiMembers) {
            val isSelf = apiMember.username.equals(currentName, ignoreCase = true) ||
                (currentEmail.isNotBlank() && apiMember.email.equals(currentEmail, ignoreCase = true))
            val index = current.indexOfFirst {
                it.id == apiMember.id ||
                it.username.equals(apiMember.username, ignoreCase = true) ||
                (apiMember.email.isNotBlank() && it.email.equals(apiMember.email, ignoreCase = true))
            }
            val updatedMember = apiMember.copy(
                isOnline = if (isSelf) true else apiMember.isOnline
            )
            if (index >= 0) {
                current[index] = updatedMember
            } else {
                current.add(updatedMember)
            }
        }

        saveMembers(context, communityId, current)
    }

    /**
     * Records a real user who joined or chatted in this community.
     */
    @Synchronized
    fun addMember(context: Context, communityId: String, member: ServerMember) {
        val current = getMembers(context, communityId, "").toMutableList()
        val index = current.indexOfFirst {
            it.id == member.id || it.username.equals(member.username, ignoreCase = true)
        }
        if (index >= 0) {
            current[index] = member
        } else {
            current.add(member)
        }
        saveMembers(context, communityId, current)
    }

    @Synchronized
    fun saveMembers(context: Context, communityId: String, members: List<ServerMember>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = normalizeKey(communityId)
        val array = JSONArray()

        for (m in members) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("username", m.username)
                put("email", m.email)
                put("handle", m.handle)
                put("role", m.role)
                put("isOnline", m.isOnline)
                put("customStatus", m.customStatus)
                put("joinedAt", m.joinedAt)
            }
            array.put(obj)
        }

        prefs.edit().putString(key, array.toString()).apply()
    }

    @Synchronized
    fun removeCommunityMembers(context: Context, communityId: String) {
        if (communityId.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = normalizeKey(communityId)
        prefs.edit().remove(key).apply()
    }

    private fun normalizeKey(raw: String): String =
        "members_" + raw.lowercase().trim().replace("[^a-z0-9_]".toRegex(), "_")
}
