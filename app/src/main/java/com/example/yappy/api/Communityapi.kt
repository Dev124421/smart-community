package com.example.yappy.api

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ApiCommunity(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val memberCount: Int,
    val isMember: Boolean
)

data class Channel(
    val id: String,
    val name: String,
    val description: String,
    val type: String
)

class ApiException(val code: Int, message: String) : Exception(message)

/**
 * Client matching the Smart Community Postman Collection.
 * All functions BLOCK, so call them from a background thread / coroutine.
 */
object CommunityApi {

    private const val BASE_URL = "https://smart-community-2.onrender.com/api"

    /** 1. GET /api/communities */
    fun getCommunities(token: String): Result<List<ApiCommunity>> = runCatching {
        val json = request("GET", "/communities", token)
        val arr = json.optJSONArray("communities") ?: JSONArray()
        (0 until arr.length()).map { parseCommunity(arr.getJSONObject(it)) }
    }

    /** 2. POST /api/communities  { name, description } */
    fun createCommunity(token: String, name: String, description: String): Result<ApiCommunity> =
        runCatching {
            val body = JSONObject().put("name", name).put("description", description)
            val json = request("POST", "/communities", token, body)
            val commObj = json.optJSONObject("community") ?: json
            parseCommunity(commObj).copy(isMember = true)
        }

    /** 3. POST /api/communities/{id}/join  (409 "already a member" counts as success) */
    fun joinCommunity(token: String, communityId: String): Result<Unit> = runCatching {
        try {
            request("POST", "/communities/$communityId/join", token)
        } catch (e: ApiException) {
            if (e.code != 409) throw e
        }
        Unit
    }

    /** 4. GET /api/channels/communities/{id}/channels */
    fun getChannels(token: String, communityId: String): Result<List<Channel>> = runCatching {
        val json = request("GET", "/channels/communities/$communityId/channels", token)
        val arr = json.optJSONArray("channels") ?: JSONArray()
        (0 until arr.length()).map { parseChannel(arr.getJSONObject(it)) }
    }

    /** 5. POST /api/channels/communities/{id}/channels  { name, description } */
    fun createChannel(
        token: String,
        communityId: String,
        name: String,
        description: String
    ): Result<Channel> = runCatching {
        val body = JSONObject().put("name", name).put("description", description)
        val json = request("POST", "/channels/communities/$communityId/channels", token, body)
        val chObj = json.optJSONObject("channel") ?: json
        parseChannel(chObj)
    }

    /** 6. GET /api/channels/{channelId} */
    fun getChannelDetails(token: String, channelId: String): Result<Channel> = runCatching {
        val json = request("GET", "/channels/$channelId", token)
        val chObj = json.optJSONObject("channel") ?: json
        parseChannel(chObj)
    }

    /** 7. PATCH /api/channels/{channelId}  { name?, description? } */
    fun updateChannel(
        token: String,
        channelId: String,
        name: String?,
        description: String?
    ): Result<Channel> = runCatching {
        val body = JSONObject().apply {
            if (name != null) put("name", name)
            if (description != null) put("description", description)
        }
        val json = request("PATCH", "/channels/$channelId", token, body)
        val chObj = json.optJSONObject("channel") ?: json
        parseChannel(chObj)
    }

    /** 8. DELETE /api/channels/{channelId} */
    fun deleteChannel(token: String, channelId: String): Result<Unit> = runCatching {
        request("DELETE", "/channels/$channelId", token)
        Unit
    }

    private fun parseCommunity(o: JSONObject) = ApiCommunity(
        id = o.optString("_id", o.optString("id")),
        name = o.optString("name"),
        description = o.optString("description"),
        category = o.optString("category", "General"),
        memberCount = o.optInt("memberCount", 0),
        isMember = o.optBoolean("isMember", false)
    )

    private fun parseChannel(o: JSONObject) = Channel(
        id = o.optString("_id", o.optString("id")),
        name = o.optString("name"),
        description = o.optString("description"),
        type = o.optString("type", "text")
    )

    private fun request(
        method: String,
        path: String,
        token: String,
        body: JSONObject? = null
    ): JSONObject {
        val conn = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 40000   // Render free tier can take a while to wake up
            readTimeout = 40000
            if (token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        try {
            if (method == "POST" || method == "PATCH" || method == "PUT") {
                conn.doOutput = true
                conn.outputStream.use { it.write((body?.toString() ?: "{}").toByteArray()) }
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = try { JSONObject(text) } catch (e: Exception) { JSONObject() }

            if (code !in 200..299) {
                val errorMsg = json.optString("message", "Request failed ($code)")
                throw ApiException(code, errorMsg)
            }
            return json
        } finally {
            conn.disconnect()
        }
    }
}
