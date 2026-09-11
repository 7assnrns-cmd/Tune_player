package com.example.lyrics.parser

import com.example.lyrics.model.LyricDocument
import com.example.lyrics.model.LyricLine
import com.example.lyrics.model.LyricRole
import com.example.lyrics.model.LyricWord

object LrcParser {

    private val METADATA_REGEX = Regex("^\\[([a-zA-Z]+):(.*)\\]$")
    private val TIME_TAG_REGEX = Regex("\\[(\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?)\\]")
    private val ENHANCED_WORD_REGEX = Regex("<(\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?)>([^<]*)")

    fun parse(rawLrc: String, songId: String = "", defaultTitle: String = "", defaultArtist: String = ""): LyricDocument {
        if (rawLrc.isBlank()) {
            return LyricDocument(songId = songId, title = defaultTitle, artist = defaultArtist)
        }

        val metadata = mutableMapOf<String, String>()
        var offsetMs = 0L

        data class RawLineEntry(val timestampMs: Long, val rawText: String)
        val rawEntries = mutableListOf<RawLineEntry>()

        rawLrc.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            val metaMatch = METADATA_REGEX.matchEntire(trimmed)
            if (metaMatch != null) {
                val key = metaMatch.groupValues[1].lowercase()
                val value = metaMatch.groupValues[2].trim()
                metadata[key] = value
                if (key == "offset") {
                    offsetMs = value.toLongOrNull() ?: 0L
                }
                return@forEach
            }

            // A line might have multiple timestamp tags: [00:12.00][00:24.00]Chorus lyrics
            val timeMatches = TIME_TAG_REGEX.findAll(trimmed).toList()
            if (timeMatches.isNotEmpty()) {
                val textContent = trimmed.substring(timeMatches.last().range.last + 1).trim()
                for (match in timeMatches) {
                    val timeStr = match.groupValues[1]
                    val timeMs = parseLrcTimestamp(timeStr) + offsetMs
                    rawEntries.add(RawLineEntry(timeMs, textContent))
                }
            }
        }

        // Sort by timestamp
        val sortedEntries = rawEntries.sortedBy { it.timestampMs }
        val lines = mutableListOf<LyricLine>()
        var globalWordIndex = 0

        for (i in sortedEntries.indices) {
            val entry = sortedEntries[i]
            val nextStartMs = if (i < sortedEntries.lastIndex) sortedEntries[i + 1].timestampMs else entry.timestampMs + 4000L
            val lineDuration = (nextStartMs - entry.timestampMs).coerceIn(1200L, 8000L)
            val lineEndMs = entry.timestampMs + lineDuration

            val isBg = (entry.rawText.startsWith("(") && entry.rawText.endsWith(")")) ||
                    (entry.rawText.startsWith("[") && entry.rawText.endsWith("]"))
            val role = if (isBg) LyricRole.BACKGROUND_VOCAL else LyricRole.MAIN_VOCAL

            // Check if enhanced word-level LRC tags exist: <00:12.30>Hello <00:12.80>World
            val enhancedMatches = ENHANCED_WORD_REGEX.findAll(entry.rawText).toList()
            val words = if (enhancedMatches.isNotEmpty()) {
                val parsedWords = mutableListOf<LyricWord>()
                for (wIdx in enhancedMatches.indices) {
                    val m = enhancedMatches[wIdx]
                    val wTime = parseLrcTimestamp(m.groupValues[1]) + offsetMs
                    val wText = m.groupValues[2]
                    val nextWTime = if (wIdx < enhancedMatches.lastIndex) {
                        parseLrcTimestamp(enhancedMatches[wIdx + 1].groupValues[1]) + offsetMs
                    } else {
                        lineEndMs
                    }
                    val wEnd = nextWTime.coerceAtLeast(wTime + 100L)
                    parsedWords.add(
                        LyricWord(
                            globalIndex = globalWordIndex++,
                            text = wText,
                            startTimeMs = wTime,
                            endTimeMs = wEnd,
                            lineIndex = i,
                            role = role,
                            isBackgroundVocal = isBg
                        )
                    )
                }
                parsedWords
            } else {
                // Synthesize word timings across the line duration
                val cleanText = entry.rawText.replace(Regex("<[^>]+>"), "")
                val tokens = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }
                if (tokens.isNotEmpty()) {
                    val perWord = lineDuration / tokens.size
                    tokens.mapIndexed { tIdx, token ->
                        val wStart = entry.timestampMs + (tIdx * perWord)
                        val wEnd = if (tIdx == tokens.lastIndex) lineEndMs else wStart + perWord
                        LyricWord(
                            globalIndex = globalWordIndex++,
                            text = if (tIdx < tokens.lastIndex) "$token " else token,
                            startTimeMs = wStart,
                            endTimeMs = wEnd,
                            lineIndex = i,
                            role = role,
                            isBackgroundVocal = isBg
                        )
                    }
                } else emptyList()
            }

            val displayText = entry.rawText.replace(Regex("<[^>]+>"), "").trim()
            if (displayText.isNotEmpty() || words.isNotEmpty()) {
                lines.add(
                    LyricLine(
                        lineIndex = i,
                        text = if (displayText.isNotEmpty()) displayText else words.joinToString(" ") { it.text },
                        startTimeMs = entry.timestampMs,
                        endTimeMs = lineEndMs,
                        words = words,
                        role = role,
                        isBackgroundVocal = isBg
                    )
                )
            }
        }

        return LyricDocument(
            songId = songId,
            title = metadata["ti"] ?: defaultTitle,
            artist = metadata["ar"] ?: defaultArtist,
            lines = lines,
            metadata = metadata,
            source = "LRC",
            hasWordTiming = lines.any { it.words.isNotEmpty() }
        )
    }

    private fun parseLrcTimestamp(timeStr: String): Long {
        try {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val minutes = parts[0].toLong()
                val secParts = parts[1].split(".", ":")
                val seconds = secParts[0].toLong()
                val fraction = if (secParts.size > 1) {
                    val rawFrac = secParts[1]
                    if (rawFrac.length == 2) rawFrac.toLong() * 10L // hundredths of a second
                    else rawFrac.padEnd(3, '0').take(3).toLong()
                } else 0L
                return (minutes * 60_000L) + (seconds * 1000L) + fraction
            }
        } catch (e: Exception) {
            // ignore
        }
        return 0L
    }
}
