package com.example.account

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class GoogleAccountManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("secure_account_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<GoogleUserProfile?>(null)
    val currentUser: StateFlow<GoogleUserProfile?> = _currentUser.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(true)
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    init {
        // Load saved session if exists
        loadSavedUser()
    }

    private fun loadSavedUser() {
        val email = prefs.getString("user_email", null)
        val name = prefs.getString("user_name", null)
        val id = prefs.getString("user_id", null)
        val avatar = prefs.getString("user_avatar", null)
        val lastSync = prefs.getLong("last_sync_time", 0L)

        if (email != null && id != null) {
            _currentUser.value = GoogleUserProfile(
                id = id,
                email = email,
                displayName = name ?: email.substringBefore("@"),
                avatarUrl = avatar,
                isYouTubeLinked = true,
                isGoogleDriveLinked = prefs.getBoolean("drive_linked", true),
                lastSyncTimestampMs = if (lastSync > 0L) lastSync else null
            )
        }
    }

    suspend fun signInWithGoogle(
        email: String = "music.enthusiast@gmail.com",
        displayName: String = "ArchiveTune Listener"
    ): Result<GoogleUserProfile> = withContext(Dispatchers.IO) {
        try {
            val user = GoogleUserProfile(
                id = "google_user_${System.currentTimeMillis() % 100000}",
                email = email,
                displayName = displayName,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                isYouTubeLinked = true,
                isGoogleDriveLinked = true,
                lastSyncTimestampMs = System.currentTimeMillis()
            )

            prefs.edit()
                .putString("user_id", user.id)
                .putString("user_email", user.email)
                .putString("user_name", user.displayName)
                .putString("user_avatar", user.avatarUrl)
                .putBoolean("drive_linked", user.isGoogleDriveLinked)
                .putLong("last_sync_time", user.lastSyncTimestampMs ?: 0L)
                .apply()

            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun switchAccount(newEmail: String, newName: String): Result<GoogleUserProfile> {
        signOut()
        return signInWithGoogle(newEmail, newName)
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        _autoSyncEnabled.value = enabled
        prefs.edit().putBoolean("auto_sync", enabled).apply()
    }

    fun updateLastSyncTime(timestampMs: Long) {
        prefs.edit().putLong("last_sync_time", timestampMs).apply()
        _currentUser.value = _currentUser.value?.copy(lastSyncTimestampMs = timestampMs)
    }
}
