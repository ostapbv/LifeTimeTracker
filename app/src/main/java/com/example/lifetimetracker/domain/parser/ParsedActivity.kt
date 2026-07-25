package com.example.lifetimetracker.domain.parser

enum class ParseConfidence {
    HIGH,
    LOW,
    FAILED
}

data class ParsedActivity(
    val durationMinutes: Int?,
    val categoryKey: String?,
    val confidence: ParseConfidence
)
