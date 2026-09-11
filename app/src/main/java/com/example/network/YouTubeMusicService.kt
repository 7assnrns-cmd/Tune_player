package com.example.network

import android.content.Context
import com.example.data.repository.YouTubeMusicRepository
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.network.api.YouTubeApiClient
import com.example.network.api.YouTubeApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

/**
 * YouTube Music Recommendations & Search Integration Service
 * Uses Retrofit-based YouTubeApiService and YouTubeMusicRepository to fetch live tracks over the network.
 */
class YouTubeMusicService(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val youTubeApiService: YouTubeApiService = YouTubeApiClient.createService(okHttpClient)
) {
    private val repository: YouTubeMusicRepository = com.example.data.repository.YouTubeMusicRepositoryImpl(
        youTubeApiService = youTubeApiService,
        okHttpClient = okHttpClient
    )

    suspend fun getRecommendations(): List<Song> = withContext(Dispatchers.IO) {
        repository.getRecommendedTracks().first()
    }

    suspend fun getRecommendedPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        repository.getFeaturedPlaylists().first()
    }

    suspend fun searchYouTubeMusic(query: String): List<Song> = withContext(Dispatchers.IO) {
        repository.searchTracks(query)
    }
}
