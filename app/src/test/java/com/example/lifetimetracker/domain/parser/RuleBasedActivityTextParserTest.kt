package com.example.lifetimetracker.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class RuleBasedActivityTextParserTest {

    private val parser = RuleBasedActivityTextParser()

    @Test
    fun testParse_hoursAndMinutes() {
        val result = parser.parse("watched YouTube for 1 hour 20 minutes")
        assertEquals(80, result.durationMinutes)
        assertEquals("social_video", result.categoryKey)
        assertEquals(ParseConfidence.HIGH, result.confidence)
    }

    @Test
    fun testParse_onlyMinutes() {
        val result = parser.parse("studied for 90 minutes")
        assertEquals(90, result.durationMinutes)
        assertEquals("learning", result.categoryKey)
        assertEquals(ParseConfidence.HIGH, result.confidence)
    }

    @Test
    fun testParse_decimalHours() {
        val result = parser.parse("worked for 1.5 hours")
        assertEquals(90, result.durationMinutes)
        assertEquals("work", result.categoryKey)
        assertEquals(ParseConfidence.HIGH, result.confidence)
    }

    @Test
    fun testParse_halfHour() {
        val result = parser.parse("clean for half an hour")
        assertEquals(30, result.durationMinutes)
        assertEquals("personal", result.categoryKey)
        assertEquals(ParseConfidence.HIGH, result.confidence)
    }

    @Test
    fun testParse_noDuration_lowConfidence() {
        val result = parser.parse("just worked a lot")
        assertEquals(null, result.durationMinutes)
        assertEquals("work", result.categoryKey)
        assertEquals(ParseConfidence.FAILED, result.confidence)
    }

    @Test
    fun testParse_noCategory_lowConfidence() {
        val result = parser.parse("did something for 45 minutes")
        assertEquals(45, result.durationMinutes)
        assertEquals(null, result.categoryKey)
        assertEquals(ParseConfidence.LOW, result.confidence)
    }
}
