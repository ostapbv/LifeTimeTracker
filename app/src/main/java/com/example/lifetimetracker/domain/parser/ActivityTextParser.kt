package com.example.lifetimetracker.domain.parser

interface ActivityTextParser {
    fun parse(text: String): ParsedActivity
}
