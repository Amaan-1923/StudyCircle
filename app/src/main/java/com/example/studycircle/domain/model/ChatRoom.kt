package com.example.studycircle.domain.model

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subject: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val memberCount: Int = 0,
    val emoji: String = "💬"
)