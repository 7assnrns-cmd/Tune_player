package com.example.backup

import android.content.Context
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.dao.SongDao
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BackupManager(
    private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val songs = songDao.getAllSongsList()
        val favorites = songs.filter { it.isFavorite }.map { it.id }

        val root = JSONObject()
        val meta = JSONObject().apply {
            put("schemaVersion", 1)
            put("exportedAt", System.currentTimeMillis())
            put("appVersion", "1.0")
            put("itemCount", songs.size)
        }
        root.put("metadata", meta)

        val favArray = JSONArray()
        favorites.forEach { favArray.put(it) }
        root.put("favoriteSongIds", favArray)

        val playlistArray = JSONArray()
        // Save real playlists
        val playlists = playlistDao.getAllPlaylistsList()
        playlists.forEach { pl ->
            val pObj = JSONObject().apply {
                put("name", pl.name)
                val songIds = playlistDao.getSongIdsForPlaylist(pl.id)
                val arr = JSONArray()
                songIds.forEach { arr.put(it) }
                put("songIds", arr)
            }
            playlistArray.put(pObj)
        }
        root.put("playlists", playlistArray)

        // Settings
        val settingsObj = JSONObject().apply {
            put("themeMode", "DARK")
            put("dynamicColor", true)
            put("audioQuality", "HIGH")
        }
        root.put("settings", settingsObj)

        val resultJson = root.toString(2)

        // Write to local backup file
        try {
            val backupFile = File(context.filesDir, "archivetune_backup.json")
            backupFile.writeText(resultJson)
        } catch (e: Exception) {
            // Ignore
        }

        resultJson
    }

    suspend fun restoreFromJson(jsonString: String, mode: RestoreConflictMode): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val meta = root.optJSONObject("metadata")
            if (meta == null) {
                return@withContext Result.failure(IllegalArgumentException("Invalid backup format: Missing metadata block."))
            }

            val schemaVersion = meta.optInt("schemaVersion", 0)
            if (schemaVersion < 1) {
                return@withContext Result.failure(IllegalArgumentException("Unsupported backup version: $schemaVersion"))
            }

            // Restore favorites
            val favArray = root.optJSONArray("favoriteSongIds")
            var restoredFavs = 0
            if (favArray != null) {
                for (i in 0 until favArray.length()) {
                    val songId = favArray.getString(i)
                    songDao.setFavorite(songId, true)
                    restoredFavs++
                }
            }

            // Restore playlists
            val playlistArray = root.optJSONArray("playlists")
            var restoredPlaylists = 0
            if (playlistArray != null) {
                for (i in 0 until playlistArray.length()) {
                    val pObj = playlistArray.getJSONObject(i)
                    val pName = pObj.getString("name")
                    val songIds = pObj.optJSONArray("songIds")

                    val playlistId = playlistDao.insertPlaylist(
                        PlaylistEntity(name = if (mode == RestoreConflictMode.MERGE) "$pName (Restored)" else pName)
                    )
                    if (songIds != null) {
                        for (sIdx in 0 until songIds.length()) {
                            val sId = songIds.getString(sIdx)
                            playlistDao.addSongToPlaylist(
                                PlaylistSongCrossRef(playlistId = playlistId, songId = sId, position = sIdx)
                            )
                        }
                    }
                    restoredPlaylists++
                }
            }

            Result.success("Restored $restoredPlaylists playlists and $restoredFavs favorites successfully.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
