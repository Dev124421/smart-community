package com.example.yappy.model

data class ServerMember(
    val id: String,
    val username: String,
    val email: String = "",
    val handle: String = "@${username.lowercase().replace(" ", "").replace("[^a-z0-9_]".toRegex(), "")}",
    val role: String = "MEMBER", // "OWNER", "ADMIN", "MEMBER", "BOT"
    val isOnline: Boolean = false,
    val customStatus: String? = null,
    val joinedAt: String = "Joined recently"
)
