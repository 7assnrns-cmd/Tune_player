package com.example.account

data class GoogleUserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val isYouTubeLinked: Boolean = true,
    val isGoogleDriveLinked: Boolean = false,
    val lastSyncTimestampMs: Long? = null
)

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class SyncReport(
    val lastSyncTimeMs: Long = System.currentTimeMillis(),
    val playlistsSynced: Int = 0,
    val favoritesSynced: Int = 0,
    val historyItemsSynced: Int = 0,
    val preferencesSynced: Int = 0,
    val conflictsResolved: Int = 0,
    val status: SyncState = SyncState.SUCCESS,
    val message: String = "Synchronization completed successfully."
)
