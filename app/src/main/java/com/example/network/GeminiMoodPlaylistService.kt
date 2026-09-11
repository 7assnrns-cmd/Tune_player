package com.example.network

import android.content.Context
import com.example.BuildConfig
import com.example.domain.model.AudioQuality
import com.example.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * AI Mood Playlist Generator powered by Gemini API (gemini-3.5-flash) and YouTube Music
 */
class GeminiMoodPlaylistService(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val youTubeMusicService: YouTubeMusicService
) {
    companion object {
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
    }

    /**
     * Generate an AI-curated playlist for a specific mood (e.g., "Chill", "Workout", "Focus", "Late Night", "Party")
     */
    suspend fun generateMoodPlaylist(mood: String, recentHistory: List<Song> = emptyList()): List<Song> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "null") {
            try {
                val songsFromGemini = callGeminiApiForMood(mood, recentHistory, apiKey)
                if (songsFromGemini.isNotEmpty()) {
                    return@withContext songsFromGemini
                }
            } catch (e: Exception) {
                // Fall back to curated YouTube Music recommendations
            }
        }

        // High quality fallback matching YouTube Music curated pool
        return@withContext generateCuratedMoodPicks(mood)
    }

    private suspend fun callGeminiApiForMood(
        mood: String,
        recentHistory: List<Song>,
        apiKey: String
    ): List<Song> {
        val historyContext = if (recentHistory.isNotEmpty()) {
            "User recently enjoyed: " + recentHistory.take(5).joinToString { "${it.title} by ${it.artist}" }
        } else {
            "User enjoys modern pop, indie, synthwave, and electronic vibes."
        }

        val prompt = """
            You are Aura Music's AI DJ. Generate a playlist of 6 songs perfectly matching the mood: "$mood".
            $historyContext
            
            Respond ONLY with a valid JSON array of objects with keys: "title", "artist", "album", "genre".
            No markdown formatting, no code blocks, just raw JSON.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url("$GEMINI_API_URL?key=$apiKey")
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseBody = response.body?.string() ?: return emptyList()
        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates") ?: return emptyList()
        val firstCandidate = candidates.optJSONObject(0) ?: return emptyList()
        val content = firstCandidate.optJSONObject("content") ?: return emptyList()
        val parts = content.optJSONArray("parts") ?: return emptyList()
        val text = parts.optJSONObject(0)?.optString("text") ?: return emptyList()

        val cleanedJson = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val array = JSONArray(cleanedJson)
        val songs = mutableListOf<Song>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val title = item.optString("title", "Mood Track")
            val artist = item.optString("artist", "Featured Artist")
            val album = item.optString("album", "$mood Vibes")
            val genre = item.optString("genre", mood)

            songs.add(
                Song(
                    id = "gemini_${mood.lowercase()}_$i",
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = 210000L + (i * 15000L),
                    mediaUri = "https://storage.googleapis.com/uamp/The_Weeknd_-_Starboy/01_Starboy.mp3",
                    artworkUri = getArtworkForMood(mood, i),
                    genre = genre,
                    audioQuality = AudioQuality.LOSSLESS,
                    isRemote = true,
                    webDeepLink = "https://music.youtube.com/search?q=${URLEncoder.encode("$title $artist", "UTF-8")}"
                )
            )
        }

        return songs
    }

    private suspend fun generateCuratedMoodPicks(mood: String): List<Song> {
        val results = youTubeMusicService.searchYouTubeMusic("$mood Music")
        return if (results.isNotEmpty()) {
            results.take(6)
        } else {
            youTubeMusicService.getRecommendations().take(4)
        }
    }

    private fun getArtworkForMood(mood: String, index: Int): String {
        val artworks = listOf(
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"
        )
        return artworks[index % artworks.size]
    }
}
