package com.example.yappy

import android.content.Context
import android.content.SharedPreferences
import com.example.yappy.api.Channel
import com.example.yappy.api.CommunityApi
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * Manages persisted channels for each server/community and syncs with CommunityApi.
 * Guarantees every community maintains a record of its channels and seeds #general
 * and #welcome-and-rules by default.
 */
object ChannelManager {

    private const val PREFS_NAME = "yappy_community_channels"

    fun getDefaultChannels(): List<Channel> = listOf(
        Channel(
            id = "default_general",
            name = "general",
            description = "General discussion and chat",
            type = "text"
        ),
        Channel(
            id = "default_welcome",
            name = "welcome-and-rules",
            description = "Welcome and server guidelines",
            type = "text"
        ),
        Channel(
            id = "default_lobby",
            name = "Lobby",
            description = "General voice lounge",
            type = "voice"
        ),
        Channel(
            id = "default_gaming",
            name = "Gaming",
            description = "Gaming voice channel",
            type = "voice"
        )
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun getChannels(context: Context, communityId: String): List<Channel> {
        val safeId = if (communityId.isBlank()) "default_community" else communityId
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString("channels_$safeId", null)

        if (jsonStr.isNullOrBlank()) {
            val defaults = getDefaultChannels()
            saveChannels(context, safeId, defaults)
            return defaults
        }

        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<Channel>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Channel(
                        id = o.optString("id"),
                        name = o.optString("name"),
                        description = o.optString("description"),
                        type = o.optString("type", "text")
                    )
                )
            }
            if (list.isEmpty()) {
                val defaults = getDefaultChannels()
                saveChannels(context, safeId, defaults)
                defaults
            } else {
                list
            }
        } catch (e: Exception) {
            val defaults = getDefaultChannels()
            saveChannels(context, safeId, defaults)
            defaults
        }
    }

    @Synchronized
    fun saveChannels(context: Context, communityId: String, channels: List<Channel>) {
        val safeId = if (communityId.isBlank()) "default_community" else communityId
        val arr = JSONArray()
        for (ch in channels) {
            val o = JSONObject()
            o.put("id", ch.id)
            o.put("name", ch.name)
            o.put("description", ch.description)
            o.put("type", ch.type)
            arr.put(o)
        }
        getPrefs(context).edit().putString("channels_$safeId", arr.toString()).apply()
    }

    @Synchronized
    fun addChannel(context: Context, communityId: String, channel: Channel) {
        val current = getChannels(context, communityId).toMutableList()
        val index = current.indexOfFirst {
            (channel.id.isNotBlank() && it.id == channel.id) ||
            it.name.trim().equals(channel.name.trim(), ignoreCase = true)
        }
        if (index >= 0) {
            current[index] = channel
        } else {
            current.add(channel)
        }
        saveChannels(context, communityId, current)
    }

    /**
     * Synchronizes community channels with CommunityApi:
     * 1. Fetches channels from API
     * 2. If remote channels are empty, creates default #general and #welcome-and-rules on server
     * 3. Merges and persists locally
     */
    fun syncWithApi(
        context: Context,
        token: String,
        communityId: String,
        onResult: (List<Channel>) -> Unit
    ) {
        val localChannels = getChannels(context, communityId)

        if (token.isBlank() || communityId.isBlank() || communityId.startsWith("local_")) {
            onResult(localChannels)
            return
        }

        thread {
            try {
                val apiResult = CommunityApi.getChannels(token, communityId)
                apiResult.onSuccess { remoteChannels ->
                    val finalChannels = if (remoteChannels.isEmpty()) {
                        CommunityApi.createChannel(token, communityId, "general", "General discussion")
                        CommunityApi.createChannel(token, communityId, "welcome-and-rules", "Welcome and server rules")
                        val refreshed = CommunityApi.getChannels(token, communityId).getOrDefault(emptyList())
                        if (refreshed.isNotEmpty()) refreshed else getDefaultChannels()
                    } else {
                        val merged = remoteChannels.toMutableList()
                        val hasVoice = merged.any { it.type.equals("voice", ignoreCase = true) }
                        if (!hasVoice) {
                            merged.add(Channel("default_lobby", "Lobby", "General voice lounge", "voice"))
                            merged.add(Channel("default_gaming", "Gaming", "Gaming voice channel", "voice"))
                        }
                        merged
                    }
                    saveChannels(context, communityId, finalChannels)
                    onResult(finalChannels)
                }.onFailure {
                    onResult(localChannels)
                }
            } catch (e: Exception) {
                onResult(localChannels)
            }
        }
    }
}
