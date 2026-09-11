package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongsList(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 30")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 30")
    fun getRecentlyAddedSongs(): Flow<List<SongEntity>>

    @Query("SELECT s.* FROM songs s INNER JOIN playback_history h ON s.id = h.songId ORDER BY h.playedAt DESC LIMIT 30")
    fun getRecentlyPlayedSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun setFavorite(songId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String)

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("DELETE FROM songs WHERE id LIKE 'demo_%' OR id LIKE 'remote_%' OR id LIKE 'fake_%'")
    suspend fun purgeFakeSongs()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    suspend fun getAllPlaylistsList(): List<PlaylistEntity>

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getSongIdsForPlaylist(playlistId: Long): List<String>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    fun getPlaylistById(id: Long): Flow<PlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName WHERE id = :id")
    suspend fun renamePlaylist(id: Long, newName: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :id")
    suspend fun deletePlaylistSongs(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)

    @Query("SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.id = ps.songId WHERE ps.playlistId = :playlistId ORDER BY ps.position ASC")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: Long): Flow<Int>

    @Transaction
    suspend fun deletePlaylistAndSongs(id: Long) {
        deletePlaylistSongs(id)
        deletePlaylist(id)
    }
}

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT 100")
    fun getRecentHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC")
    suspend fun getAllHistory(): List<PlaybackHistoryEntity>
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearches()
}
