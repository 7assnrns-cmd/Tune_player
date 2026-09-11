package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.AudioQuality
import com.example.domain.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String?,
    val trackNumber: Int,
    val year: Int,
    val genre: String,
    val isFavorite: Boolean,
    val playCount: Int,
    val dateAdded: Long,
    val audioQuality: String = AudioQuality.HIGH.name,
    val isRemote: Boolean = false
) {
    fun toSong(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            mediaUri = mediaUri,
            artworkUri = artworkUri,
            trackNumber = trackNumber,
            year = year,
            genre = genre,
            isFavorite = isFavorite,
            playCount = playCount,
            dateAdded = dateAdded,
            audioQuality = try { AudioQuality.valueOf(audioQuality) } catch (e: Exception) { AudioQuality.HIGH },
            isRemote = isRemote
        )
    }

    companion object {
        fun fromSong(song: Song): SongEntity {
            return SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                durationMs = song.durationMs,
                mediaUri = song.mediaUri,
                artworkUri = song.artworkUri,
                trackNumber = song.trackNumber,
                year = song.year,
                genre = song.genre,
                isFavorite = song.isFavorite,
                playCount = song.playCount,
                dateAdded = song.dateAdded,
                audioQuality = song.audioQuality.name,
                isRemote = song.isRemote
            )
        }
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val artworkUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: String,
    val position: Int
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val positionMs: Long = 0L
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)
