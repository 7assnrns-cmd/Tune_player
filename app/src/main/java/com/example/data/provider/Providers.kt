package com.example.data.provider

import com.example.data.mediastore.MediaStoreScanner
import com.example.domain.model.AudioQuality
import com.example.domain.model.Song

interface MusicProvider {
    val providerId: String
    val providerName: String
    suspend fun search(query: String): List<Song>
    suspend fun getStreamUrl(song: Song, quality: AudioQuality): String?
    fun isPlaybackSupported(song: Song): Boolean = true
    fun getMetadataUrl(song: Song): String? = null
}

class LocalMusicProvider(
    private val scanner: MediaStoreScanner
) : MusicProvider {
    override val providerId: String = "local_provider"
    override val providerName: String = "Device Music"

    override suspend fun search(query: String): List<Song> {
        val all = scanner.scanLocalAudio()
        return all.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getStreamUrl(song: Song, quality: AudioQuality): String {
        return song.mediaUri
    }

    override fun isPlaybackSupported(song: Song): Boolean = true
}
