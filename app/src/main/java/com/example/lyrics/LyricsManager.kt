package com.example.lyrics

import android.content.Context
import com.example.lyrics.model.LyricDocument
import com.example.lyrics.parser.LrcParser
import com.example.lyrics.parser.TtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class LyricsManager(private val context: Context) {

    private val lyricsCache = mutableMapOf<String, LyricDocument>()
    private val _currentDocument = MutableStateFlow<LyricDocument?>(null)
    val currentDocument: StateFlow<LyricDocument?> = _currentDocument.asStateFlow()

    suspend fun getLyricsForSong(
        songId: String,
        title: String,
        artist: String,
        lyricsText: String? = null
    ): LyricDocument? = withContext(Dispatchers.IO) {
        lyricsCache[songId]?.let { return@withContext it }

        // 1. If explicit raw lyrics provided
        if (!lyricsText.isNullOrBlank()) {
            val doc = parseRawLyrics(lyricsText, songId, title, artist)
            lyricsCache[songId] = doc
            return@withContext doc
        }

        // 2. Check local disk cache
        val cacheFile = File(context.cacheDir, "lyrics_${songId.hashCode()}.json")
        if (cacheFile.exists()) {
            try {
                val savedText = cacheFile.readText()
                val doc = parseRawLyrics(savedText, songId, title, artist)
                lyricsCache[songId] = doc
                return@withContext doc
            } catch (e: Exception) {
                // Ignore read error
            }
        }

        // No real lyrics available for this track
        null
    }

    suspend fun setLyricsForSong(songId: String, title: String, artist: String, rawContent: String): LyricDocument = withContext(Dispatchers.IO) {
        val doc = parseRawLyrics(rawContent, songId, title, artist)
        lyricsCache[songId] = doc
        _currentDocument.value = doc
        try {
            val cacheFile = File(context.cacheDir, "lyrics_${songId.hashCode()}.json")
            cacheFile.writeText(rawContent)
        } catch (e: Exception) {
            // Ignore
        }
        doc
    }

    fun setActiveSongLyrics(doc: LyricDocument?) {
        _currentDocument.value = doc
    }

    private fun parseRawLyrics(raw: String, songId: String, title: String, artist: String): LyricDocument {
        val trimmed = raw.trim()
        return when {
            trimmed.startsWith("<tt", ignoreCase = true) || trimmed.contains("<p begin=") -> {
                TtmlParser.parse(trimmed, songId, title, artist)
            }
            trimmed.contains("[00:") || trimmed.contains("[01:") || trimmed.contains("[ti:") -> {
                LrcParser.parse(trimmed, songId, title, artist)
            }
            else -> {
                // Plain text: assign arbitrary readable timestamps
                LrcParser.parse(convertPlainTextToLrc(trimmed), songId, title, artist)
            }
        }
    }

    private fun convertPlainTextToLrc(plain: String): String {
        val lines = plain.lines().filter { it.isNotBlank() }
        var currentSec = 3
        return buildString {
            lines.forEach { line ->
                val mm = currentSec / 60
                val ss = currentSec % 60
                appendLine(String.format("[%02d:%02d.00]%s", mm, ss, line))
                currentSec += 4
            }
        }
    }
}
