package com.example.lifetimetracker.domain.parser

import javax.inject.Inject
import kotlin.math.roundToInt

class RuleBasedActivityTextParser @Inject constructor() : ActivityTextParser {

    private val categoryKeywords = mapOf(
        "work" to listOf("work", "worked", "meeting", "code", "coding", "job", "email"),
        "learning" to listOf("study", "studied", "learn", "learned", "course", "read", "reading", "book", "school"),
        "personal" to listOf("personal", "gym", "workout", "clean", "cleaned", "cook", "cooked", "run", "ran", "sleep", "slept", "eat", "ate"),
        "social_video" to listOf("youtube", "tiktok", "social", "instagram", "video", "movie", "tv", "watch", "watched"),
        "other" to listOf("other", "misc")
    )

    override fun parse(text: String): ParsedActivity {
        val lowerText = text.lowercase()

        val duration = extractDurationMinutes(lowerText)
        val categoryKey = extractCategoryKey(lowerText)

        val confidence = when {
            duration != null && categoryKey != null -> ParseConfidence.HIGH
            duration != null -> ParseConfidence.LOW // Missing category, but has duration
            else -> ParseConfidence.FAILED
        }

        return ParsedActivity(
            durationMinutes = duration,
            categoryKey = categoryKey,
            confidence = confidence
        )
    }

    private fun extractCategoryKey(text: String): String? {
        val words = text.split("\\W+".toRegex())
        for (word in words) {
            for ((key, keywords) in categoryKeywords) {
                if (keywords.contains(word)) {
                    return key
                }
            }
        }
        return null
    }

    private fun extractDurationMinutes(text: String): Int? {
        var totalMinutes = 0

        // Match patterns like "X hour(s)" or "X.Y hour(s)"
        val hoursRegex = Regex("([0-9]*\\.?[0-9]+)\\s*hour")
        val hoursMatch = hoursRegex.find(text)
        if (hoursMatch != null) {
            val hours = hoursMatch.groupValues[1].toDoubleOrNull()
            if (hours != null) {
                totalMinutes += (hours * 60).roundToInt()
            }
        } else if (text.contains("half an hour") || text.contains("half hour")) {
            totalMinutes += 30
        } else if (text.contains("an hour") || text.contains("one hour")) {
            totalMinutes += 60
        }

        // Match patterns like "X minute(s)" or "X min(s)"
        val minutesRegex = Regex("([0-9]+)\\s*min")
        val minutesMatch = minutesRegex.find(text)
        if (minutesMatch != null) {
            val minutes = minutesMatch.groupValues[1].toIntOrNull()
            if (minutes != null) {
                totalMinutes += minutes
            }
        }

        return if (totalMinutes > 0) totalMinutes else null
    }
}
