package com.example.data.provider

import com.example.domain.model.AudioQuality
import com.example.domain.model.Song
import com.example.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class YouTubeSearchResult(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val durationMs: Long,
    val isOfficialMusicVideo: Boolean = true
) {
    fun toSong(): Song {
        return Song(
            id = "yt_$videoId",
            title = title,
            artist = channelTitle,
            album = "YouTube Music",
            durationMs = durationMs,
            mediaUri = "https://music.youtube.com/watch?v=$videoId",
            artworkUri = thumbnailUrl,
            genre = "YouTube",
            audioQuality = AudioQuality.HIGH,
            isRemote = true,
            isPlaybackAvailable = false,
            webDeepLink = "https://music.youtube.com/watch?v=$videoId"
        )
    }

    val youtubeMusicUrl: String get() = "https://music.youtube.com/watch?v=$videoId"
    val youtubeVideoUrl: String get() = "https://www.youtube.com/watch?v=$videoId"
}

class YouTubeMusicProvider(
    private val networkClient: NetworkClient,
    private val apiKeyProvider: () -> String? = { null }
) : MusicProvider {

    override val providerId: String = "youtube_music"
    override val providerName: String = "YouTube Music (Official Integration)"

    override suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext emptyList()

        val apiKey = apiKeyProvider()
        if (!apiKey.isNullOrBlank()) {
            val apiResult = networkClient.executeWithRetry(
                maxAttempts = 2,
                requestDescription = "YouTube Data API Search"
            ) {
                fetchFromYouTubeDataApi(trimmedQuery, apiKey)
            }
            return@withContext apiResult.getOrNull() ?: emptyList()
        }

        // When no API key is provided, never fabricate fake songs; return empty list
        emptyList()
    }

    override suspend fun getStreamUrl(song: Song, quality: AudioQuality): String {
        return song.mediaUri
    }

    private suspend fun fetchFromYouTubeDataApi(query: String, apiKey: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&videoCategoryId=10&maxResults=15&q=$encodedQuery&key=$apiKey"
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build()

            val response = networkClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = org.json.JSONObject(body)
            val items = json.optJSONArray("items") ?: return@withContext emptyList()

            val results = mutableListOf<Song>()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val idObj = item.optJSONObject("id") ?: continue
                val videoId = idObj.optString("videoId")
                if (videoId.isNullOrBlank()) continue

                val snippet = item.optJSONObject("snippet") ?: continue
                val title = snippet.optString("title", "Unknown Track")
                val channelTitle = snippet.optString("channelTitle", "Unknown Artist")
                val thumbnails = snippet.optJSONObject("thumbnails")
                val thumbUrl = thumbnails?.optJSONObject("high")?.optString("url")
                    ?: thumbnails?.optJSONObject("default")?.optString("url")

                results.add(
                    Song(
                        id = "yt_$videoId",
                        title = title,
                        artist = channelTitle,
                        album = "YouTube Music",
                        durationMs = 0L,
                        mediaUri = "https://music.youtube.com/watch?v=$videoId",
                        artworkUri = thumbUrl,
                        genre = "YouTube",
                        audioQuality = AudioQuality.HIGH,
                        isRemote = true,
                        isPlaybackAvailable = false,
                        webDeepLink = "https://music.youtube.com/watch?v=$videoId"
                    )
                )
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }
}
