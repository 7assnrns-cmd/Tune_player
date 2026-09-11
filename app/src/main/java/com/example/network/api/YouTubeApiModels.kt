package com.example.network.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "etag") val etag: String? = null,
    @Json(name = "nextPageToken") val nextPageToken: String? = null,
    @Json(name = "items") val items: List<YouTubeSearchResultItem>? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchResultItem(
    @Json(name = "id") val id: YouTubeResourceId? = null,
    @Json(name = "snippet") val snippet: YouTubeSnippet? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeResourceId(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "playlistId") val playlistId: String? = null,
    @Json(name = "channelId") val channelId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoListResponse(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "etag") val etag: String? = null,
    @Json(name = "nextPageToken") val nextPageToken: String? = null,
    @Json(name = "items") val items: List<YouTubeVideoItem>? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "snippet") val snippet: YouTubeSnippet? = null,
    @Json(name = "contentDetails") val contentDetails: YouTubeContentDetails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistResponse(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "etag") val etag: String? = null,
    @Json(name = "nextPageToken") val nextPageToken: String? = null,
    @Json(name = "items") val items: List<YouTubePlaylistItem>? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "snippet") val snippet: YouTubeSnippet? = null,
    @Json(name = "contentDetails") val contentDetails: YouTubePlaylistContentDetails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippet(
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "channelId") val channelId: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnails? = null,
    @Json(name = "channelTitle") val channelTitle: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnails(
    @Json(name = "default") val defaultThumb: YouTubeThumbnailInfo? = null,
    @Json(name = "medium") val medium: YouTubeThumbnailInfo? = null,
    @Json(name = "high") val high: YouTubeThumbnailInfo? = null,
    @Json(name = "standard") val standard: YouTubeThumbnailInfo? = null,
    @Json(name = "maxres") val maxres: YouTubeThumbnailInfo? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailInfo(
    @Json(name = "url") val url: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeContentDetails(
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "dimension") val dimension: String? = null,
    @Json(name = "definition") val definition: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistContentDetails(
    @Json(name = "itemCount") val itemCount: Int? = null
)
