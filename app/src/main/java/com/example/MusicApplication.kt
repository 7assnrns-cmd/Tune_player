package com.example

import android.app.Application
import com.example.data.local.DataStoreManager
import com.example.data.local.MusicDatabase
import com.example.data.mediastore.MediaStoreScanner
import com.example.data.repository.MusicRepository
import com.example.data.repository.MusicRepositoryImpl
import com.example.data.repository.YouTubeMusicRepository
import com.example.data.repository.YouTubeMusicRepositoryImpl
import com.example.media.player.MusicPlayerManager
import com.example.network.GeminiMoodPlaylistService
import com.example.network.NetworkClient
import com.example.network.YouTubeMusicService
import com.example.network.api.YouTubeApiClient
import com.example.network.api.YouTubeApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MusicApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { MusicDatabase.getInstance(this) }
    val scanner by lazy { MediaStoreScanner(this) }
    val dataStoreManager by lazy { DataStoreManager(this) }
    val networkClient by lazy { NetworkClient(this) }

    val youTubeApiService: YouTubeApiService by lazy {
        YouTubeApiClient.createService(networkClient.okHttpClient)
    }

    val youTubeRepository: YouTubeMusicRepository by lazy {
        YouTubeMusicRepositoryImpl(
            youTubeApiService = youTubeApiService,
            okHttpClient = networkClient.okHttpClient
        )
    }

    val youTubeMusicService by lazy {
        YouTubeMusicService(this, networkClient.okHttpClient)
    }

    val geminiMoodService by lazy {
        GeminiMoodPlaylistService(this, networkClient.okHttpClient, youTubeMusicService)
    }

    val repository: MusicRepository by lazy {
        MusicRepositoryImpl(
            songDao = database.songDao(),
            playlistDao = database.playlistDao(),
            historyDao = database.historyDao(),
            searchHistoryDao = database.searchHistoryDao(),
            scanner = scanner,
            youTubeMusicService = youTubeMusicService,
            geminiMoodService = geminiMoodService
        )
    }

    val playerManager: MusicPlayerManager by lazy {
        MusicPlayerManager(this) { song ->
            applicationScope.launch {
                repository.recordPlayback(song.id, 0L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Sync local media on app launch
        applicationScope.launch {
            repository.syncLocalMedia()
        }
    }
}
