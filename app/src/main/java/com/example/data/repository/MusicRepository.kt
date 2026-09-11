package com.example.data.repository

import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.dao.SearchHistoryDao
import com.example.data.local.dao.SongDao
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.SongEntity
import com.example.data.mediastore.MediaStoreScanner
import com.example.data.provider.LocalMusicProvider
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    fun getRecentlyPlayedSongs(): Flow<List<Song>>
    fun getRecentlyAddedSongs(): Flow<List<Song>>
    fun getMostPlayedSongs(): Flow<List<Song>>
    fun getAllAlbums(): Flow<List<Album>>
    fun getAllArtists(): Flow<List<Artist>>
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String): Long
    suspend fun renamePlaylist(id: Long, newName: String)
    suspend fun deletePlaylist(id: Long)
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    suspend fun toggleFavorite(songId: String, currentStatus: Boolean)
    suspend fun recordPlayback(songId: String, positionMs: Long)
    suspend fun clearPlaybackHistory()
    fun getSearchHistory(): Flow<List<String>>
    suspend fun recordSearch(query: String)
    suspend fun clearSearchHistory()
    suspend fun deleteSearchHistory(query: String)
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun syncLocalMedia(): Int
    suspend fun getSongById(id: String): Song?
    fun getYouTubeRecommendations(): Flow<List<Song>>
    fun getYouTubePlaylists(): Flow<List<Playlist>>
    suspend fun searchYouTubeMusic(query: String): List<Song>
    suspend fun generateMoodPlaylist(mood: String): List<Song>
}

class MusicRepositoryImpl(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val historyDao: HistoryDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val scanner: MediaStoreScanner,
    private val localProvider: LocalMusicProvider = LocalMusicProvider(scanner),
    private val youTubeMusicService: com.example.network.YouTubeMusicService? = null,
    private val geminiMoodService: com.example.network.GeminiMoodPlaylistService? = null
) : MusicRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { list -> list.map { it.toSong() } }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return songDao.getFavoriteSongs().map { list -> list.map { it.toSong() } }
    }

    override fun getRecentlyPlayedSongs(): Flow<List<Song>> {
        return songDao.getRecentlyPlayedSongs().map { list -> list.map { it.toSong() } }
    }

    override fun getRecentlyAddedSongs(): Flow<List<Song>> {
        return songDao.getRecentlyAddedSongs().map { list -> list.map { it.toSong() } }
    }

    override fun getMostPlayedSongs(): Flow<List<Song>> {
        return songDao.getMostPlayedSongs().map { list -> list.map { it.toSong() } }
    }

    override fun getAllAlbums(): Flow<List<Album>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.album to it.artist }.map { (key, albumSongs) ->
                val (albumName, artistName) = key
                val firstSong = albumSongs.first()
                Album(
                    id = "album_${albumName}_$artistName",
                    title = albumName,
                    artist = artistName,
                    songCount = albumSongs.size,
                    artworkUri = firstSong.artworkUri,
                    year = firstSong.year
                )
            }.sortedBy { it.title }
        }.flowOn(Dispatchers.Default)
    }

    override fun getAllArtists(): Flow<List<Artist>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
                val uniqueAlbums = artistSongs.map { it.album }.distinct().size
                Artist(
                    id = "artist_$artistName",
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = uniqueAlbums
                )
            }.sortedBy { it.name }
        }.flowOn(Dispatchers.Default)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    artworkUri = entity.artworkUri,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { list -> list.map { it.toSong() } }
    }

    override suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun renamePlaylist(id: Long, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(id, newName)
    }

    override suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistAndSongs(id)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: String) = withContext(Dispatchers.IO) {
        val currentSongs = playlistDao.getSongsForPlaylist(playlistId).first()
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                position = currentSongs.size
            )
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun toggleFavorite(songId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        songDao.setFavorite(songId, !currentStatus)
    }

    override suspend fun recordPlayback(songId: String, positionMs: Long) = withContext(Dispatchers.IO) {
        songDao.incrementPlayCount(songId)
        historyDao.insertHistory(
            PlaybackHistoryEntity(
                songId = songId,
                positionMs = positionMs
            )
        )
    }

    override suspend fun clearPlaybackHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    override fun getSearchHistory(): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches().map { list ->
            list.map { it.query }
        }
    }

    override suspend fun recordSearch(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query.trim()))
        }
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        searchHistoryDao.clearSearches()
    }

    override suspend fun deleteSearchHistory(query: String) = withContext(Dispatchers.IO) {
        searchHistoryDao.deleteSearch(query)
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        if (query.isBlank()) {
            return getAllSongs()
        }
        return songDao.searchSongs(query).map { list -> list.map { it.toSong() } }
    }

    override suspend fun syncLocalMedia(): Int = withContext(Dispatchers.IO) {
        // Purge any fake/demo songs to ensure only real media is preserved
        songDao.purgeFakeSongs()

        val scanned = scanner.scanLocalAudio()
        if (scanned.isNotEmpty()) {
            // Preserve favorite status if already in database
            val existing = songDao.getAllSongs().first().associateBy { it.id }
            val entities = scanned.map { song ->
                val existingEntity = existing[song.id]
                if (existingEntity != null) {
                    SongEntity.fromSong(
                        song.copy(
                            isFavorite = existingEntity.isFavorite,
                            playCount = existingEntity.playCount
                        )
                    )
                } else {
                    SongEntity.fromSong(song)
                }
            }
            songDao.insertSongs(entities)
        }
        scanned.size
    }

    override suspend fun getSongById(id: String): Song? = withContext(Dispatchers.IO) {
        songDao.getSongById(id)?.toSong()
    }

    override fun getYouTubeRecommendations(): Flow<List<Song>> = kotlinx.coroutines.flow.flow {
        val picks = youTubeMusicService?.getRecommendations() ?: emptyList()
        emit(picks)
    }.flowOn(Dispatchers.IO)

    override fun getYouTubePlaylists(): Flow<List<Playlist>> = kotlinx.coroutines.flow.flow {
        val playlists = youTubeMusicService?.getRecommendedPlaylists() ?: emptyList()
        emit(playlists)
    }.flowOn(Dispatchers.IO)

    override suspend fun searchYouTubeMusic(query: String): List<Song> = withContext(Dispatchers.IO) {
        youTubeMusicService?.searchYouTubeMusic(query) ?: emptyList()
    }

    override suspend fun generateMoodPlaylist(mood: String): List<Song> = withContext(Dispatchers.IO) {
        val recentHistory = songDao.getRecentlyPlayedSongs().first().map { it.toSong() }
        geminiMoodService?.generateMoodPlaylist(mood, recentHistory)
            ?: youTubeMusicService?.getRecommendations()
            ?: emptyList()
    }
}
