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
 * Plain HttpURLConnection client for the Smart Community API.
 * All functions BLOCK, so call them from a background thread.
 */
object CommunityApi {

    private const val BASE_URL = "https://smart-community-2.onrender.com/api"

    // ---------- Communities ----------

    /** GET /api/communities */
    fun getCommunities(token: String): Result<List<ApiCommunity>> = runCatching {
        val json = request("GET", "/communities", token)
        val arr = json.optJSONArray("communities") ?: JSONArray()
        (0 until arr.length()).map { parseCommunity(arr.getJSONObject(it)) }
    }

    /** POST /api/communities  { name, description } */
    fun createCommunity(token: String, name: String, description: String): Result<ApiCommunity> =
        runCatching {
            val body = JSONObject().put("name", name).put("description", description)
            val json = request("POST", "/communities", token, body)
            parseCommunity(json.getJSONObject("community")).copy(isMember = true)
        }

    /** POST /api/communities/{id}/join  (409 "already a member" counts as success) */
    fun joinCommunity(token: String, communityId: String): Result<Unit> = runCatching {
        try {
            request("POST", "/communities/$communityId/join", token)
        } catch (e: ApiException) {
            if (e.code != 409) throw e
        }
        Unit
    }

    // ---------- Channels ----------

    /** GET /api/channels/communities/{id}/channels */
    fun getChannels(token: String, communityId: String): Result<List<Channel>> = runCatching {
        val json = request("GET", "/channels/communities/$communityId/channels", token)
        val arr = json.optJSONArray("channels") ?: JSONArray()
        (0 until arr.length()).map { parseChannel(arr.getJSONObject(it)) }
    }

    /** POST /api/channels/communities/{id}/channels  { name, description } (owner only) */
    fun createChannel(
        token: String,
        communityId: String,
        name: String,
        description: String
    ): Result<Channel> = runCatching {
        val body = JSONObject().put("name", name).put("description", description)
        val json = request("POST", "/channels/communities/$communityId/channels", token, body)
        parseChannel(json.getJSONObject("channel"))
    }

    // ---------- Helpers ----------

    private fun parseCommunity(o: JSONObject) = ApiCommunity(
        id = o.getString("_id"),
        name = o.optString("name"),
        description = o.optString("description"),
        category = o.optString("category"),
        memberCount = o.optInt("memberCount", 0),
        isMember = o.optBoolean("isMember", false)
    )

    private fun parseChannel(o: JSONObject) = Channel(
        id = o.getString("_id"),
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
            setRequestProperty("Authorization", "Bearer $token")
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
                throw ApiException(code, json.optString("message", "Request failed ($code)"))
            }
            return json
        } finally {
            conn.disconnect()
        }
    }
}