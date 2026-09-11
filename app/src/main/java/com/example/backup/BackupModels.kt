package com.example.backup

import com.example.domain.model.Song
import java.security.MessageDigest

data class BackupMetadata(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val checksum: String = ""
)

data class BackupPlaylist(
    val name: String,
    val songIds: List<String>
)

data class BackupPayload(
    val metadata: BackupMetadata,
    val playlists: List<BackupPlaylist> = emptyList(),
    val favoriteSongIds: List<String> = emptyList(),
    val settingsMap: Map<String, String> = emptyMap(),
    val totalPlayCount: Int = 0
) {
    fun calculateChecksum(): String {
        val raw = "${metadata.schemaVersion}_${playlists.size}_${favoriteSongIds.size}_${settingsMap.size}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}

enum class RestoreConflictMode {
    MERGE,
    REPLACE
}
