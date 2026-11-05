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

object CommunityApi {

    private const val BASE_URL = "https://smart-community-2.onrender.com/api"

    fun getCommunities(token: String): Result<List<ApiCommunity>> = runCatching {
        val json = request("GET", "/communities", token)
        val arr = json.optJSONArray("communities") ?: JSONArray()
        (0 until arr.length()).map { parseCommunity(arr.getJSONObject(it)) }
    }

    fun createCommunity(token: String, name: String, description: String): Result<ApiCommunity> =
        runCatching {
            val body = JSONObject().put("name", name).put("description", description)
            val json = request("POST", "/communities", token, body)
            val commObj = json.optJSONObject("community") ?: json
            parseCommunity(commObj).copy(isMember = true)
        }

    fun joinCommunity(token: String, communityId: String): Result<Unit> = runCatching {
        try {
            request("POST", "/communities/$communityId/join", token)
        } catch (e: ApiException) {
            if (e.code != 409) throw e
        }
        Unit
    }

    fun getChannels(token: String, communityId: String): Result<List<Channel>> = runCatching {
        val json = request("GET", "/channels/communities/$communityId/channels", token)
        val arr = json.optJSONArray("channels") ?: JSONArray()
        (0 until arr.length()).map { parseChannel(arr.getJSONObject(it)) }
    }

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

    fun getChannelDetails(token: String, channelId: String): Result<Channel> = runCatching {
        val json = request("GET", "/channels/$channelId", token)
        val chObj = json.optJSONObject("channel") ?: json
        parseChannel(chObj)
    }

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
            connectTimeout = 40000
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
