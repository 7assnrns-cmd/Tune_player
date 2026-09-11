package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.repository.MusicRepository
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.AudioQuality
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.SleepTimerOption
import com.example.domain.model.Song
import com.example.media.player.MusicPlayerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModel(
    private val repository: MusicRepository,
    private val playerManager: MusicPlayerManager,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState

    val allSongs: StateFlow<List<Song>> = repository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedSongs: StateFlow<List<Song>> = repository.getRecentlyPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAddedSongs: StateFlow<List<Song>> = repository.getRecentlyAddedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayedSongs: StateFlow<List<Song>> = repository.getMostPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = repository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = repository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedSongs: StateFlow<List<Song>> = repository.getYouTubeRecommendations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedPlaylists: StateFlow<List<Playlist>> = repository.getYouTubePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently viewed playlist ID (for playlist details screen)
    val selectedPlaylistId = MutableStateFlow<Long?>(null)

    val selectedPlaylistSongs: StateFlow<List<Song>> = selectedPlaylistId
        .flatMapLatest { id ->
            if (id != null) repository.getPlaylistSongs(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    val searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<Song>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else repository.searchSongs(query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory: StateFlow<List<String>> = repository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    val themeMode: StateFlow<String> = dataStoreManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    val dynamicColor: StateFlow<Boolean> = dataStoreManager.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val audioQualityPref: StateFlow<String> = dataStoreManager.audioQuality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "HIGH")

    val librarySort: StateFlow<String> = dataStoreManager.librarySort
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "TITLE")

    val gaplessPlayback: StateFlow<Boolean> = dataStoreManager.gapless
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val crossfade: StateFlow<Boolean> = dataStoreManager.crossfade
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI Dialog & Overlay States
    private val _isFullPlayerVisible = MutableStateFlow(false)
    val isFullPlayerVisible: StateFlow<Boolean> = _isFullPlayerVisible.asStateFlow()

    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

    private val _isSleepTimerDialogVisible = MutableStateFlow(false)
    val isSleepTimerDialogVisible: StateFlow<Boolean> = _isSleepTimerDialogVisible.asStateFlow()

    private val _isCreatePlaylistDialogVisible = MutableStateFlow(false)
    val isCreatePlaylistDialogVisible: StateFlow<Boolean> = _isCreatePlaylistDialogVisible.asStateFlow()

    private val _songForAddToPlaylist = MutableStateFlow<Song?>(null)
    val songForAddToPlaylist: StateFlow<Song?> = _songForAddToPlaylist.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _aiMoodSongs = MutableStateFlow<List<Song>>(emptyList())
    val aiMoodSongs: StateFlow<List<Song>> = _aiMoodSongs.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    fun generateAiMoodPlaylist(mood: String) {
        viewModelScope.launch {
            _isAiGenerating.value = true
            val songs = repository.generateMoodPlaylist(mood)
            _aiMoodSongs.value = songs
            _isAiGenerating.value = false
        }
    }

    fun toggleKaraokeMode() {
        playerManager.toggleKaraokeMode()
    }

    // Playback Controls
    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val targetQueue = if (queue.isNotEmpty()) queue else allSongs.value
        playerManager.playSong(song, targetQueue)
        viewModelScope.launch {
            repository.recordPlayback(song.id, 0L)
        }
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isNotEmpty()) {
            playerManager.playPlaylist(songs, startIndex)
            viewModelScope.launch {
                repository.recordPlayback(songs[startIndex].id, 0L)
            }
        }
    }

    fun togglePlayPause() = playerManager.togglePlayPause()

    fun skipToNext() = playerManager.skipToNext()

    fun skipToPrevious() = playerManager.skipToPrevious()

    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)

    fun fastForward() = playerManager.fastForward(30000L)

    fun rewind() = playerManager.rewind(10000L)

    fun toggleShuffle() = playerManager.toggleShuffle()

    fun cycleRepeatMode() = playerManager.cycleRepeatMode()

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
        viewModelScope.launch {
            dataStoreManager.setPlaybackSpeed(speed)
        }
    }

    fun setAudioQuality(quality: AudioQuality) {
        playerManager.setAudioQuality(quality)
        viewModelScope.launch {
            dataStoreManager.setAudioQuality(quality.name)
        }
    }

    fun reorderQueue(from: Int, to: Int) = playerManager.reorderQueue(from, to)

    fun removeFromQueue(index: Int) = playerManager.removeFromQueue(index)

    fun addToQueue(song: Song) = playerManager.addToQueue(song)

    fun clearQueue() = playerManager.clearQueue()

    fun startSleepTimer(option: SleepTimerOption) {
        playerManager.startSleepTimer(option)
        _isSleepTimerDialogVisible.value = false
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
        _isSleepTimerDialogVisible.value = false
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun createPlaylist(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.createPlaylist(name.trim())
                _isCreatePlaylistDialogVisible.value = false
            }
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                repository.renamePlaylist(id, newName.trim())
            }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
            if (selectedPlaylistId.value == id) {
                selectedPlaylistId.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _songForAddToPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun selectPlaylist(playlistId: Long?) {
        selectedPlaylistId.value = playlistId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun submitSearch(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repository.recordSearch(query)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun deleteSearchQuery(query: String) {
        viewModelScope.launch {
            repository.deleteSearchHistory(query)
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            repository.syncLocalMedia()
            _isScanning.value = false
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setDynamicColor(enabled)
        }
    }

    fun setGapless(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setGapless(enabled)
        }
    }

    fun setCrossfade(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setCrossfade(enabled)
        }
    }

    fun setLibrarySort(sort: String) {
        viewModelScope.launch {
            dataStoreManager.setLibrarySort(sort)
        }
    }

    fun clearPlaybackHistory() {
        viewModelScope.launch {
            repository.clearPlaybackHistory()
        }
    }

    fun showFullPlayer(show: Boolean) {
        _isFullPlayerVisible.value = show
    }

    fun showQueueSheet(show: Boolean) {
        _isQueueSheetVisible.value = show
    }

    fun showSleepTimerDialog(show: Boolean) {
        _isSleepTimerDialogVisible.value = show
    }

    fun showCreatePlaylistDialog(show: Boolean) {
        _isCreatePlaylistDialogVisible.value = show
    }

    fun showAddToPlaylistDialog(song: Song?) {
        _songForAddToPlaylist.value = song
    }
}

class MusicViewModelFactory(
    private val repository: MusicRepository,
    private val playerManager: MusicPlayerManager,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(repository, playerManager, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
