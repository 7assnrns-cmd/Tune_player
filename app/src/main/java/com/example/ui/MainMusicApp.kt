package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.Playlist
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.DraggableGlassOverlay
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.rememberDraggableOverlayState
import com.example.ui.navigation.Screen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.player.FullPlayerScreen
import com.example.ui.screens.player.QueueBottomSheet
import com.example.ui.screens.playlist.PlaylistDetailScreen
import com.example.ui.screens.playlist.PlaylistsScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.GlassDock
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.rememberAnimatedDynamicPalette
import com.example.ui.viewmodel.MusicViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMusicApp(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val recentlyPlayedSongs by viewModel.recentlyPlayedSongs.collectAsState()
    val recentlyAddedSongs by viewModel.recentlyAddedSongs.collectAsState()
    val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsState()
    val recommendedSongs by viewModel.recommendedSongs.collectAsState()
    val recommendedPlaylists by viewModel.recommendedPlaylists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsState()
    val selectedPlaylistSongs by viewModel.selectedPlaylistSongs.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    val isScanning by viewModel.isScanning.collectAsState()

    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsState()
    val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsState()
    val isSleepTimerDialogVisible by viewModel.isSleepTimerDialogVisible.collectAsState()
    val isCreatePlaylistDialogVisible by viewModel.isCreatePlaylistDialogVisible.collectAsState()
    val songForAddToPlaylist by viewModel.songForAddToPlaylist.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Dynamic Color Extraction with AndroidX Palette (500-800ms smooth animation)
    val dynamicPalette = rememberAnimatedDynamicPalette(playbackState.currentSong)

    // Frosted Glass Haze Blur State
    val hazeState = remember { HazeState() }

    // Reversible & Interruptible Settings Overlay State (0.0 to 1.0)
    val settingsOverlayState = rememberDraggableOverlayState(initiallyOpen = false)

    // Reversible & Interruptible Search Glass Overlay State (0.0 to 1.0)
    val searchOverlayState = rememberDraggableOverlayState(initiallyOpen = false)

    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Back handling for overlays and sub-views (interruptible reversal)
    BackHandler(enabled = isFullPlayerVisible || selectedPlaylistId != null || settingsOverlayState.isVisible || searchOverlayState.isVisible) {
        if (isFullPlayerVisible) {
            viewModel.showFullPlayer(false)
        } else if (selectedPlaylistId != null) {
            viewModel.selectPlaylist(null)
        } else if (searchOverlayState.isVisible) {
            coroutineScope.launch { searchOverlayState.close() }
        } else if (settingsOverlayState.isVisible) {
            coroutineScope.launch { settingsOverlayState.close() }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(dynamicPalette.backgroundBrush)
    ) {
        // Main Screen Navigation & Content (Target for frosted background blur)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .haze(hazeState)
        ) {
            if (selectedPlaylistId != null) {
                val currentPlaylist = (playlists + recommendedPlaylists).find { it.id == selectedPlaylistId }
                if (currentPlaylist != null) {
                    val currentSongs = if (selectedPlaylistSongs.isNotEmpty()) selectedPlaylistSongs else recommendedSongs
                    PlaylistDetailScreen(
                        playlist = currentPlaylist,
                        songs = currentSongs,
                        playbackState = playbackState,
                        onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                        onRemoveSongFromPlaylist = { songId ->
                            viewModel.removeSongFromPlaylist(currentPlaylist.id, songId)
                        },
                        onAddToQueue = { song -> viewModel.addToQueue(song) },
                        onDeletePlaylist = { viewModel.deletePlaylist(currentPlaylist.id) },
                        onBackClick = { viewModel.selectPlaylist(null) }
                    )
                }
            } else {
                // Tab Navigation: AnimatedContent with Fade + Slide transitions
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val getOrder: (Screen) -> Int = { screen ->
                            when (screen) {
                                Screen.Home -> 0
                                Screen.Library -> 1
                                Screen.Search -> 2
                                Screen.Playlists -> 3
                                Screen.Settings -> 4
                            }
                        }
                        val isForward = getOrder(targetState) > getOrder(initialState)
                        (slideInHorizontally(
                            initialOffsetX = { if (isForward) it / 3 else -it / 3 },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { if (isForward) -it / 3 else it / 3 },
                                animationSpec = tween(350, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(200))
                        )
                    },
                    label = "tab_navigation_transition"
                ) { screen ->
                    when (screen) {
                        Screen.Home -> HomeScreen(
                            playbackState = playbackState,
                            allSongs = allSongs,
                            favoriteSongs = favoriteSongs,
                            recentlyPlayedSongs = recentlyPlayedSongs,
                            recentlyAddedSongs = recentlyAddedSongs,
                            mostPlayedSongs = mostPlayedSongs,
                            recommendedSongs = recommendedSongs,
                            recommendedPlaylists = recommendedPlaylists,
                            albums = albums,
                            isScanning = isScanning,
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onAddToPlaylist = { song -> viewModel.showAddToPlaylistDialog(song) },
                            onAddToQueue = { song -> viewModel.addToQueue(song) },
                            onAlbumClick = { album ->
                                val albumSongs = allSongs.filter { it.album == album.title }
                                if (albumSongs.isNotEmpty()) viewModel.playSong(albumSongs.first(), albumSongs)
                            },
                            onPlaylistClick = { playlist -> viewModel.selectPlaylist(playlist.id) },
                            onRescanClick = { viewModel.rescanLibrary() },
                            onOpenSearchOverlay = {
                                coroutineScope.launch { searchOverlayState.open() }
                            },
                            onNavigateToLibrary = { currentScreen = Screen.Library },
                            onMoodSelect = { mood ->
                                viewModel.generateAiMoodPlaylist(mood)
                            }
                        )
                        Screen.Search -> SearchScreen(
                            searchQuery = searchQuery,
                            searchResults = (searchResults + recommendedSongs).distinctBy { it.id },
                            searchHistory = searchHistory,
                            playbackState = playbackState,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onSubmitSearch = { viewModel.submitSearch(it) },
                            onDeleteSearch = { viewModel.deleteSearchQuery(it) },
                            onClearSearchHistory = { viewModel.clearSearchHistory() },
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onAddToPlaylist = { song -> viewModel.showAddToPlaylistDialog(song) },
                            onAddToQueue = { song -> viewModel.addToQueue(song) }
                        )
                        Screen.Library -> LibraryScreen(
                            playbackState = playbackState,
                            allSongs = allSongs,
                            favoriteSongs = favoriteSongs,
                            albums = albums,
                            artists = artists,
                            isScanning = isScanning,
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onAddToPlaylist = { song -> viewModel.showAddToPlaylistDialog(song) },
                            onAddToQueue = { song -> viewModel.addToQueue(song) },
                            onAlbumClick = { album ->
                                val albumSongs = allSongs.filter { it.album == album.title }
                                if (albumSongs.isNotEmpty()) viewModel.playSong(albumSongs.first(), albumSongs)
                            },
                            onArtistClick = { artist ->
                                val artistSongs = allSongs.filter { it.artist == artist.name }
                                if (artistSongs.isNotEmpty()) viewModel.playSong(artistSongs.first(), artistSongs)
                            },
                            onRescanClick = { viewModel.rescanLibrary() }
                        )
                        Screen.Playlists -> PlaylistsScreen(
                            playlists = (playlists + recommendedPlaylists).distinctBy { it.id },
                            onPlaylistClick = { playlist -> viewModel.selectPlaylist(playlist.id) },
                            onCreatePlaylistClick = { viewModel.showCreatePlaylistDialog(true) },
                            onDeletePlaylist = { playlist -> viewModel.deletePlaylist(playlist.id) }
                        )
                        Screen.Settings -> {
                            SettingsScreen(
                                onNavigateBackToLibrary = { currentScreen = Screen.Library }
                            )
                        }
                    }
                }
            }
        }

        // Floating Bottom Controls: MiniPlayer + Floating GlassDock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating Frosted MiniPlayer (Right above the dock)
            MiniPlayer(
                currentSong = playbackState.currentSong,
                isPlaying = playbackState.isPlaying,
                currentPositionMs = playbackState.currentPositionMs,
                durationMs = playbackState.durationMs,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipToNext() },
                onClick = { viewModel.showFullPlayer(true) },
                hazeState = hazeState
            )

            // Floating Pill-shaped Glass Dock (16.dp bottom elevated, highly translucent)
            GlassDock(
                currentScreen = currentScreen,
                onSelectScreen = { screen ->
                    viewModel.selectPlaylist(null)
                    currentScreen = screen
                },
                onOpenSearch = {
                    coroutineScope.launch {
                        if (searchOverlayState.isOpen) {
                            searchOverlayState.close()
                        } else {
                            searchOverlayState.open()
                        }
                    }
                },
                onOpenSettings = {
                    coroutineScope.launch {
                        if (settingsOverlayState.isOpen) {
                            settingsOverlayState.close()
                        } else {
                            settingsOverlayState.open()
                        }
                    }
                },
                hazeState = hazeState,
                isSettingsOpen = settingsOverlayState.isVisible,
                isSearchOpen = searchOverlayState.isVisible
            )
        }

        // ==============================================================
        // INTERRUPTIBLE & REVERSIBLE SEARCH GLASS OVERLAY (0.0 TO 1.0)
        // ==============================================================
        DraggableGlassOverlay(
            state = searchOverlayState,
            title = "Search & Discover",
            onDismissRequest = {
                coroutineScope.launch { searchOverlayState.close() }
            },
            hazeState = hazeState
        ) {
            SearchScreen(
                searchQuery = searchQuery,
                searchResults = (searchResults + recommendedSongs).distinctBy { it.id },
                searchHistory = searchHistory,
                playbackState = playbackState,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onSubmitSearch = { viewModel.submitSearch(it) },
                onDeleteSearch = { viewModel.deleteSearchQuery(it) },
                onClearSearchHistory = { viewModel.clearSearchHistory() },
                onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                onAddToPlaylist = { song -> viewModel.showAddToPlaylistDialog(song) },
                onAddToQueue = { song -> viewModel.addToQueue(song) }
            )
        }

        // ==============================================================
        // INTERRUPTIBLE & REVERSIBLE SETTINGS GLASS OVERLAY (0.0 TO 1.0)
        // ==============================================================
        DraggableGlassOverlay(
            state = settingsOverlayState,
            title = "Settings & Customization",
            onDismissRequest = {
                coroutineScope.launch { settingsOverlayState.close() }
            },
            hazeState = hazeState
        ) {
            SettingsScreen(
                onNavigateBackToLibrary = {
                    coroutineScope.launch { settingsOverlayState.close() }
                }
            )
        }

        // Full Player Modal Screen
        AnimatedVisibility(
            visible = isFullPlayerVisible && playbackState.currentSong != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            FullPlayerScreen(
                playbackState = playbackState,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipToNext() },
                onSkipPrevious = { viewModel.skipToPrevious() },
                onSeek = { pos -> viewModel.seekTo(pos) },
                onFastForward = { viewModel.fastForward() },
                onRewind = { viewModel.rewind() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onCycleRepeatMode = { viewModel.cycleRepeatMode() },
                onSetPlaybackSpeed = { speed -> viewModel.setPlaybackSpeed(speed) },
                onSetAudioQuality = { quality -> viewModel.setAudioQuality(quality) },
                onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                onOpenQueue = { viewModel.showQueueSheet(true) },
                onOpenSleepTimer = { viewModel.showSleepTimerDialog(true) },
                onDismiss = { viewModel.showFullPlayer(false) },
                hazeState = hazeState,
                onToggleKaraokeMode = { viewModel.toggleKaraokeMode() },
                onSelectSleepTimerOption = { option -> viewModel.startSleepTimer(option) },
                onCancelSleepTimer = { viewModel.cancelSleepTimer() }
            )
        }

        // Queue Bottom Sheet
        if (isQueueSheetVisible) {
            QueueBottomSheet(
                queue = playbackState.queue,
                currentQueueIndex = playbackState.currentQueueIndex,
                isPlaying = playbackState.isPlaying,
                onSelectSong = { song -> viewModel.playSong(song, playbackState.queue) },
                onRemoveFromQueue = { index -> viewModel.removeFromQueue(index) },
                onReorderQueue = { from, to -> viewModel.reorderQueue(from, to) },
                onClearQueue = { viewModel.clearQueue() },
                onDismiss = {
                    coroutineScope.launch { queueSheetState.hide() }
                    viewModel.showQueueSheet(false)
                },
                sheetState = queueSheetState
            )
        }

        // Sleep Timer Dialog
        if (isSleepTimerDialogVisible) {
            SleepTimerDialog(
                currentRemainingMs = playbackState.sleepTimerRemainingMs,
                onSelectOption = { option -> viewModel.startSleepTimer(option) },
                onCancelTimer = { viewModel.cancelSleepTimer() },
                onDismiss = { viewModel.showSleepTimerDialog(false) }
            )
        }

        // Create Playlist Dialog
        if (isCreatePlaylistDialogVisible) {
            CreatePlaylistDialog(
                onConfirm = { name -> viewModel.createPlaylist(name) },
                onDismiss = { viewModel.showCreatePlaylistDialog(false) }
            )
        }

        // Add to Playlist Dialog
        if (songForAddToPlaylist != null) {
            AddToPlaylistDialog(
                song = songForAddToPlaylist!!,
                playlists = playlists,
                onSelectPlaylist = { playlist ->
                    viewModel.addSongToPlaylist(playlist.id, songForAddToPlaylist!!.id)
                },
                onCreateNewPlaylist = {
                    viewModel.showAddToPlaylistDialog(null)
                    viewModel.showCreatePlaylistDialog(true)
                },
                onDismiss = { viewModel.showAddToPlaylistDialog(null) }
            )
        }
    }
}
