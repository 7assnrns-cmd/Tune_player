package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.data.repository.YouTubeMusicRepository
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.media.player.MusicPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Dedicated HomeViewModel for fetching real YouTube Music recommendations and handling home state.
 */
class HomeViewModel(
    private val youTubeRepository: YouTubeMusicRepository,
    private val musicRepository: MusicRepository,
    private val playerManager: MusicPlayerManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState

    val recommendedSongs: StateFlow<List<Song>> = youTubeRepository.getRecommendedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredPlaylists: StateFlow<List<Playlist>> = youTubeRepository.getFeaturedPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLocalSongs: StateFlow<List<Song>> = musicRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val targetQueue = if (queue.isNotEmpty()) queue else recommendedSongs.value
        playerManager.playSong(song, targetQueue)
        viewModelScope.launch {
            musicRepository.recordPlayback(song.id, 0L)
        }
    }

    fun togglePlayPause() = playerManager.togglePlayPause()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun rescanLocalLibrary() {
        viewModelScope.launch {
            _isRefreshing.value = true
            musicRepository.syncLocalMedia()
            _isRefreshing.value = false
        }
    }
}

class HomeViewModelFactory(
    private val youTubeRepository: YouTubeMusicRepository,
    private val musicRepository: MusicRepository,
    private val playerManager: MusicPlayerManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(youTubeRepository, musicRepository, playerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
