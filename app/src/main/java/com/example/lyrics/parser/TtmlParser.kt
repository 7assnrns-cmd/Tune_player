package com.example.lyrics.parser

import android.util.Xml
import com.example.lyrics.model.LyricDocument
import com.example.lyrics.model.LyricLine
import com.example.lyrics.model.LyricRole
import com.example.lyrics.model.LyricSinger
import com.example.lyrics.model.LyricWord
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

object TtmlParser {

    fun parse(rawTtml: String, songId: String = "", title: String = "", artist: String = ""): LyricDocument {
        if (rawTtml.isBlank()) {
            return LyricDocument(songId = songId, title = title, artist = artist)
        }

        val lines = mutableListOf<LyricLine>()
        val metadata = mutableMapOf<String, String>()
        var globalWordIndex = 0

        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(rawTtml))

            var eventType = parser.eventType
            var currentLineIndex = 0

            var currentParagraphBegin: Long? = null
            var currentParagraphEnd: Long? = null
            var currentRole = LyricRole.MAIN_VOCAL
            var currentAgent: String? = null
            val currentWords = mutableListOf<LyricWord>()
            val currentLineText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name.lowercase()) {
                            "title" -> {
                                parser.next()
                                if (parser.eventType == XmlPullParser.TEXT) {
                                    metadata["title"] = parser.text.trim()
                                }
                            }
                            "metadata" -> {
                                // Container for metadata
                            }
                            "p" -> {
                                currentWords.clear()
                                currentLineText.clear()
                                val beginAttr = parser.getAttributeValue(null, "begin")
                                val endAttr = parser.getAttributeValue(null, "end")
                                val durAttr = parser.getAttributeValue(null, "dur")
                                val roleAttr = parser.getAttributeValue(null, "ttm:role") ?: parser.getAttributeValue(null, "role")
                                val agentAttr = parser.getAttributeValue(null, "ttm:agent") ?: parser.getAttributeValue(null, "agent")

                                currentParagraphBegin = beginAttr?.let { parseTime(it) } ?: 0L
                                currentParagraphEnd = when {
                                    endAttr != null -> parseTime(endAttr)
                                    durAttr != null -> (currentParagraphBegin ?: 0L) + parseTime(durAttr)
                                    else -> null
                                }

                                currentRole = if (roleAttr?.contains("background", ignoreCase = true) == true) {
                                    LyricRole.BACKGROUND_VOCAL
                                } else {
                                    LyricRole.MAIN_VOCAL
                                }
                                currentAgent = agentAttr
                            }
                            "span" -> {
                                val beginAttr = parser.getAttributeValue(null, "begin")
                                val endAttr = parser.getAttributeValue(null, "end")
                                val durAttr = parser.getAttributeValue(null, "dur")
                                val roleAttr = parser.getAttributeValue(null, "ttm:role") ?: parser.getAttributeValue(null, "role")

                                val wordBegin = beginAttr?.let { parseTime(it) }
                                val wordEnd = when {
                                    endAttr != null -> parseTime(endAttr)
                                    durAttr != null && wordBegin != null -> wordBegin + parseTime(durAttr)
                                    else -> null
                                }

                                val spanRole = if (roleAttr?.contains("background", ignoreCase = true) == true) {
                                    LyricRole.BACKGROUND_VOCAL
                                } else {
                                    currentRole
                                }

                                parser.next()
                                if (parser.eventType == XmlPullParser.TEXT) {
                                    val wordText = parser.text
                                    currentLineText.append(wordText)

                                    val isBg = spanRole == LyricRole.BACKGROUND_VOCAL ||
                                            wordText.trim().startsWith("(") ||
                                            wordText.trim().startsWith("[")

                                    val start = wordBegin ?: currentParagraphBegin ?: 0L
                                    val end = wordEnd ?: (start + 500L)

                                    currentWords.add(
                                        LyricWord(
                                            globalIndex = globalWordIndex++,
                                            text = wordText,
                                            startTimeMs = start,
                                            endTimeMs = end,
                                            lineIndex = currentLineIndex,
                                            role = spanRole,
                                            singer = currentAgent?.let { LyricSinger(it, it) },
                                            isBackgroundVocal = isBg
                                        )
                                    )
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text
                        if (currentParagraphBegin != null && text.isNotBlank()) {
                            currentLineText.append(text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.lowercase() == "p") {
                            val lineText = currentLineText.toString().trim()
                            if (lineText.isNotEmpty() || currentWords.isNotEmpty()) {
                                val lineStart = currentParagraphBegin ?: currentWords.firstOrNull()?.startTimeMs ?: 0L
                                val lineEnd = currentParagraphEnd ?: currentWords.lastOrNull()?.endTimeMs ?: (lineStart + 3000L)

                                val isBg = currentRole == LyricRole.BACKGROUND_VOCAL ||
                                        (lineText.startsWith("(") && lineText.endsWith(")")) ||
                                        (lineText.startsWith("[") && lineText.endsWith("]"))

                                // Synthesize word tokens if spans were absent in this <p>
                                val finalWords = if (currentWords.isEmpty() && lineText.isNotEmpty()) {
                                    synthesizeWordsForLine(
                                        text = lineText,
                                        startMs = lineStart,
                                        endMs = lineEnd,
                                        lineIndex = currentLineIndex,
                                        startIndex = globalWordIndex,
                                        role = currentRole,
                                        isBg = isBg
                                    ).also { globalWordIndex += it.size }
                                } else {
                                    normalizeWordTimings(currentWords, lineStart, lineEnd)
                                }

                                lines.add(
                                    LyricLine(
                                        lineIndex = currentLineIndex++,
                                        text = if (lineText.isNotEmpty()) lineText else finalWords.joinToString(" ") { it.text },
                                        startTimeMs = lineStart,
                                        endTimeMs = lineEnd,
                                        words = finalWords,
                                        role = currentRole,
                                        singer = currentAgent?.let { LyricSinger(it, it) },
                                        isBackgroundVocal = isBg
                                    )
                                )
                            }
                            currentParagraphBegin = null
                            currentParagraphEnd = null
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Fallback: regex-based fallback if XML parser hits unusual tags
            return fallbackRegexParse(rawTtml, songId, title, artist)
        }

        val hasWordTiming = lines.any { it.words.isNotEmpty() }
        return LyricDocument(
            songId = songId,
            title = metadata["title"] ?: title,
            artist = artist,
            lines = lines.sortedBy { it.startTimeMs },
            metadata = metadata,
            source = "TTML",
            hasWordTiming = hasWordTiming
        )
    }

    private fun synthesizeWordsForLine(
        text: String,
        startMs: Long,
        endMs: Long,
        lineIndex: Int,
        startIndex: Int,
        role: LyricRole,
        isBg: Boolean
    ): List<LyricWord> {
        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()

        val totalDuration = (endMs - startMs).coerceAtLeast(100L)
        val perWordDuration = totalDuration / tokens.size

        return tokens.mapIndexed { idx, token ->
            val wStart = startMs + (idx * perWordDuration)
            val wEnd = if (idx == tokens.lastIndex) endMs else wStart + perWordDuration
            LyricWord(
                globalIndex = startIndex + idx,
                text = if (idx < tokens.lastIndex) "$token " else token,
                startTimeMs = wStart,
                endTimeMs = wEnd,
                lineIndex = lineIndex,
                role = role,
                isBackgroundVocal = isBg
            )
        }
    }

    private fun normalizeWordTimings(words: List<LyricWord>, lineStart: Long, lineEnd: Long): List<LyricWord> {
        if (words.isEmpty()) return emptyList()
        val sorted = words.sortedBy { it.startTimeMs }
        val result = mutableListOf<LyricWord>()

        for (i in sorted.indices) {
            val current = sorted[i]
            val nextStart = if (i < sorted.lastIndex) sorted[i + 1].startTimeMs else lineEnd
            val normalizedEnd = if (current.endTimeMs <= current.startTimeMs) {
                nextStart.coerceAtLeast(current.startTimeMs + 100L)
            } else {
                current.endTimeMs.coerceAtMost(nextStart.coerceAtLeast(current.startTimeMs + 50L))
            }
            result.add(current.copy(endTimeMs = normalizedEnd))
        }
        return result
    }

    fun parseTime(timeStr: String): Long {
        val trimmed = timeStr.trim()
        try {
            // Format: "12.34s"
            if (trimmed.endsWith("s", ignoreCase = true)) {
                return (trimmed.dropLast(1).toDouble() * 1000).toLong()
            }
            // Format: "1500ms"
            if (trimmed.endsWith("ms", ignoreCase = true)) {
                return trimmed.dropLast(2).toLong()
            }

            // Standard colon format: "hh:mm:ss.xxx" or "mm:ss.xxx" or "mm:ss:xx"
            val parts = trimmed.split(":")
            return when (parts.size) {
                3 -> {
                    val hours = parts[0].toLong()
                    val minutes = parts[1].toLong()
                    val secParts = parts[2].split(".", ",")
                    val seconds = secParts[0].toLong()
                    val millis = if (secParts.size > 1) {
                        secParts[1].padEnd(3, '0').take(3).toLong()
                    } else 0L
                    (hours * 3600_000L) + (minutes * 60_000L) + (seconds * 1000L) + millis
                }
                2 -> {
                    val minutes = parts[0].toLong()
                    val secParts = parts[1].split(".", ",")
                    val seconds = secParts[0].toLong()
                    val millis = if (secParts.size > 1) {
                        secParts[1].padEnd(3, '0').take(3).toLong()
                    } else 0L
                    (minutes * 60_000L) + (seconds * 1000L) + millis
                }
                1 -> {
                    (parts[0].toDouble() * 1000).toLong()
                }
                else -> 0L
            }
        } catch (e: Exception) {
            return 0L
        }
    }

    private fun fallbackRegexParse(rawTtml: String, songId: String, title: String, artist: String): LyricDocument {
        val pRegex = Regex("<p[^>]*begin=\"([^\"]+)\"[^>]*end=\"([^\"]+)\"[^>]*>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        val lines = mutableListOf<LyricLine>()
        var lineIndex = 0
        var globalWordIndex = 0

        pRegex.findAll(rawTtml).forEach { match ->
            val beginStr = match.groupValues[1]
            val endStr = match.groupValues[2]
            val innerHtml = match.groupValues[3]

            val startMs = parseTime(beginStr)
            val endMs = parseTime(endStr)

            // Strip spans for plain line text
            val cleanText = innerHtml.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
            if (cleanText.isNotBlank()) {
                val isBg = cleanText.startsWith("(") && cleanText.endsWith(")")
                val words = synthesizeWordsForLine(
                    text = cleanText,
                    startMs = startMs,
                    endMs = endMs,
                    lineIndex = lineIndex,
                    startIndex = globalWordIndex,
                    role = if (isBg) LyricRole.BACKGROUND_VOCAL else LyricRole.MAIN_VOCAL,
                    isBg = isBg
                )
                globalWordIndex += words.size

                lines.add(
                    LyricLine(
                        lineIndex = lineIndex++,
                        text = cleanText,
                        startTimeMs = startMs,
                        endTimeMs = endMs,
                        words = words,
                        role = if (isBg) LyricRole.BACKGROUND_VOCAL else LyricRole.MAIN_VOCAL,
                        isBackgroundVocal = isBg
                    )
                )
            }
        }

        return LyricDocument(
            songId = songId,
            title = title,
            artist = artist,
            lines = lines.sortedBy { it.startTimeMs },
            source = "TTML (Fallback)",
            hasWordTiming = lines.any { it.words.isNotEmpty() }
        )
    }
}
