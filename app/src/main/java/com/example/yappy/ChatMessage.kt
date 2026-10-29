package com.example.yappy.model

data class ChatMessage(
    val id: Long,
    val text: String,
    val isMine: Boolean,
    val time: String,
    val senderName: String = if (isMine) "You" else "User"
)
