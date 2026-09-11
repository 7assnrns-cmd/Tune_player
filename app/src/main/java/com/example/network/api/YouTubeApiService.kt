package com.example.network.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Official YouTube Data API v3 Retrofit Interface
 */
interface YouTubeApiService {

    /**
     * Search YouTube Music videos
     */
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10", // 10 = Music Category
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    /**
     * Search YouTube Music playlists
     */
    @GET("youtube/v3/search")
    suspend fun searchPlaylists(
        @Query("part") part: String = "snippet",
        @Query("q") query: String = "Top Music Hits",
        @Query("type") type: String = "playlist",
        @Query("maxResults") maxResults: Int = 15,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    /**
     * Fetch Most Popular / Trending Music Videos
     */
    @GET("youtube/v3/videos")
    suspend fun getMostPopularMusicVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("chart") chart: String = "mostPopular",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): YouTubeVideoListResponse

    /**
     * Get Details for specific video IDs
     */
    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YouTubeVideoListResponse
}
