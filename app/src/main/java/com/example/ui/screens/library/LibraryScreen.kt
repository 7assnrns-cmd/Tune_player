package com.example.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.PlaybackState
import com.example.domain.model.Song
import com.example.ui.components.AlbumCard
import com.example.ui.components.ArtistCard
import com.example.ui.components.SongListItem

enum class LibrarySort(val label: String) {
    TITLE("Title (A-Z)"),
    ARTIST("Artist (A-Z)"),
    DATE_ADDED("Recently Added"),
    DURATION("Duration")
}

@Composable
fun LibraryScreen(
    playbackState: PlaybackState,
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    isScanning: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Albums", "Artists", "Favorites")
    var selectedSort by remember { mutableStateOf(LibrarySort.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }

    val sortedSongs = remember(allSongs, selectedSort) {
        when (selectedSort) {
            LibrarySort.TITLE -> allSongs.sortedBy { it.title.lowercase() }
            LibrarySort.ARTIST -> allSongs.sortedBy { it.artist.lowercase() }
            LibrarySort.DATE_ADDED -> allSongs.sortedByDescending { it.dateAdded }
            LibrarySort.DURATION -> allSongs.sortedByDescending { it.durationMs }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allSongs.size} tracks • ${albums.size} albums",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.size(48.dp).testTag("library_sort_btn")
                    ) {
                        Icon(
                            Icons.Filled.Sort,
                            contentDescription = "Sort library",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        LibrarySort.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.label) },
                                onClick = {
                                    selectedSort = sort
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRescanClick,
                    modifier = Modifier.size(48.dp).testTag("library_rescan_btn")
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Scan library",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Action Buttons Row (Play All, Shuffle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val list = if (selectedTabIndex == 3) favoriteSongs else sortedSongs
                    if (list.isNotEmpty()) onSongClick(list.first(), list)
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("library_play_all_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Play All")
            }

            FilledTonalButton(
                onClick = {
                    val list = if (selectedTabIndex == 3) favoriteSongs else sortedSongs
                    if (list.isNotEmpty()) {
                        val shuffled = list.shuffled()
                        onSongClick(shuffled.first(), shuffled)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("library_shuffle_btn")
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Shuffle")
            }
        }

        // Content per Tab
        when (selectedTabIndex) {
            0 -> {
                // Songs List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(sortedSongs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isCurrentSong = song.id == playbackState.currentSong?.id,
                            isPlaying = playbackState.isPlaying && song.id == playbackState.currentSong?.id,
                            onClick = { onSongClick(song, sortedSongs) },
                            onToggleFavorite = onToggleFavorite,
                            onAddToPlaylist = onAddToPlaylist,
                            onAddToQueue = onAddToQueue,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
            1 -> {
                // Albums Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
            }
            2 -> {
                // Artists Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(artists, key = { it.id }) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = { onArtistClick(artist) }
                        )
                    }
                }
            }
            3 -> {
                // Favorites List
                if (favoriteSongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No favorite tracks yet.\nTap the heart on any song to add it!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(favoriteSongs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isCurrentSong = song.id == playbackState.currentSong?.id,
                                isPlaying = playbackState.isPlaying && song.id == playbackState.currentSong?.id,
                                onClick = { onSongClick(song, favoriteSongs) },
                                onToggleFavorite = onToggleFavorite,
                                onAddToPlaylist = onAddToPlaylist,
                                onAddToQueue = onAddToQueue,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
