package com.example.domain.model

enum class AudioQuality(val label: String, val description: String) {
    LOSSLESS("Lossless", "FLAC • 24-bit / 96kHz"),
    HIGH("High", "320 kbps • MP3 / AAC"),
    MEDIUM("Medium", "192 kbps • Standard"),
    AUTO("Auto", "Adaptive stream based on network")
}

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class SleepTimerOption(val label: String, val minutes: Int) {
    MIN_5("5 Minutes", 5),
    MIN_10("10 Minutes", 10),
    MIN_15("15 Minutes", 15),
    MIN_30("30 Minutes", 30),
    MIN_45("45 Minutes", 45),
    MIN_60("60 Minutes", 60),
    END_OF_TRACK("End of track", -1)
}

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String? = null,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val genre: String = "Music",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val isRemote: Boolean = false,
    val isPlaybackAvailable: Boolean = true,
    val webDeepLink: String? = null
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val songCount: Int,
    val artworkUri: String? = null,
    val year: Int = 0
)

data class Artist(
    val id: String,
    val name: String,
    val songCount: Int,
    val albumCount: Int = 1
)

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songCount: Int = 0,
    val durationMs: Long = 0,
    val artworkUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleMode: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val sleepTimerRemainingMs: Long? = null,
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val isBuffering: Boolean = false,
    val isKaraokeMode: Boolean = false,
    val isCrossfading: Boolean = false,
    val visualizerAmplitudes: List<Float> = emptyList(),
    val errorMessage: String? = null
)
