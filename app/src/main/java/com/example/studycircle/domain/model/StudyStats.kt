package com.example.studycircle.domain.model

data class StudyStats(
    val totalPosts: Int = 0,
    val totalHoursStudied: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val subjectDistribution: Map<String, Int> = emptyMap(),
    val weeklyActivity: List<Float> = emptyList(),
    val monthlyActivity: List<Float> = emptyList()
)

data class DailyActivity(
    val day: String = "",
    val hours: Float = 0f
)