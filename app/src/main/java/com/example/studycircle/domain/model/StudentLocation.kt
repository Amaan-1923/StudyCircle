package com.example.studycircle.domain.model

data class StudentLocation(
    val uid: String = "",
    val name: String = "",
    val subject: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)