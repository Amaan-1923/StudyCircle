package com.example.studycircle.domain.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val subject: String = "",
    val content: String = "",
    val likes: Int = 0,
    val commentCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val hasAttachment: Boolean = false,
    val attachmentName: String = ""
)