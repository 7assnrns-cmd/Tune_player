package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Album
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.ui.components.AlbumCard
import com.example.ui.components.ArtworkImage
import com.example.ui.components.SongListItem
import com.example.ui.theme.GlassButton
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassTheme

@Composable
fun HomeScreen(
    playbackState: PlaybackState,
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    recentlyPlayedSongs: List<Song>,
    recentlyAddedSongs: List<Song>,
    mostPlayedSongs: List<Song>,
    recommendedSongs: List<Song>,
    recommendedPlaylists: List<Playlist>,
    albums: List<Album>,
    isScanning: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onRescanClick: () -> Unit,
    onOpenSearchOverlay: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onMoodSelect: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentSong = playbackState.currentSong
    val isPlaying = playbackState.isPlaying

    val moodList = listOf(
        "⚡ Workout",
        "🌙 Late Night",
        "☕ Chill & Lofi",
        "🎯 Deep Focus",
        "🔥 Party Energy",
        "🌧️ Melancholy"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 140.dp)
    ) {
        // App Header: Clean & Modern with Aura Music branding (Search bar removed from top)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aura Music",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassTheme.ActiveDockIcon.copy(alpha = 0.20f))
                                .border(1.dp, GlassTheme.ActiveDockIcon.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HI-FI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GlassTheme.ActiveDockIcon,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        text = "High-Fidelity Glassmorphic Audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp,
                            color = GlassTheme.ActiveDockIcon
                        )
                    } else {
                        IconButton(
                            onClick = onRescanClick,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, GlassTheme.BorderGlass, CircleShape)
                                .testTag("home_rescan_btn")
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Scan for music",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Play Hero Glass Card (Featured / Now Playing)
        val featuredSong = currentSong ?: recentlyPlayedSongs.firstOrNull() ?: recommendedSongs.firstOrNull() ?: allSongs.firstOrNull()
        if (featuredSong != null) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("hero_quick_play_card"),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0xFF0F172A).copy(alpha = 0.55f),
                    borderColor = GlassTheme.BorderGlassHighlight,
                    elevation = 16.dp,
                    onClick = { onSongClick(featuredSong, allSongs.ifEmpty { recommendedSongs }) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(8.dp, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(18.dp))
                        ) {
                            ArtworkImage(
                                artworkUri = featuredSong.artworkUri,
                                contentDescription = "${featuredSong.title} cover",
                                modifier = Modifier.size(72.dp),
                                cornerRadius = 18.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentSong != null) "NOW PLAYING" else "QUICK PLAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GlassTheme.ActiveDockIcon,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = featuredSong.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = featuredSong.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GlassTheme.ActiveDockIcon)
                                .shadow(8.dp, CircleShape)
                                .clickable { onSongClick(featuredSong, allSongs.ifEmpty { recommendedSongs }) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF090B10),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==============================================================
        // AI MOOD DJ (Gemini Powered Mood Playlists)
        // ==============================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Mood Playlists",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Gemini AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA855F7),
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(moodList) { mood ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1B4B).copy(alpha = 0.6f))
                                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { onMoodSelect?.invoke(mood) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = mood,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ==============================================================
        // RECOMMENDED FOR YOU (YouTube Music Integration)
        // ==============================================================
        if (recommendedSongs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = GlassTheme.ActiveDockIcon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recommended for You",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "YouTube Music",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.50f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recommendedSongs, key = { it.id }) { song ->
                        GlassCard(
                            modifier = Modifier
                                .width(160.dp)
                                .testTag("rec_song_${song.id}"),
                            shape = RoundedCornerShape(22.dp),
                            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.45f),
                            borderColor = GlassTheme.BorderGlass,
                            elevation = 8.dp,
                            onClick = { onSongClick(song, recommendedSongs) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(16.dp))
                                ) {
                                    ArtworkImage(
                                        artworkUri = song.artworkUri,
                                        contentDescription = song.title,
                                        modifier = Modifier.size(140.dp),
                                        cornerRadius = 16.dp
                                    )

                                    // Top right badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = song.genre,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.90f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Floating Quick Play button on hover/corner
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(GlassTheme.ActiveDockIcon)
                                            .clickable { onSongClick(song, recommendedSongs) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color(0xFF090B10),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.70f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==============================================================
        // CURATED YOUTUBE MUSIC PLAYLISTS
        // ==============================================================
        if (recommendedPlaylists.isNotEmpty()) {
            item {
                SectionHeader(title = "Featured Playlists", onSeeAll = onNavigateToLibrary)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recommendedPlaylists, key = { it.id }) { playlist ->
                        GlassCard(
                            modifier = Modifier
                                .width(150.dp)
                                .testTag("curated_playlist_${playlist.id}"),
                            shape = RoundedCornerShape(22.dp),
                            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.40f),
                            borderColor = GlassTheme.BorderGlass,
                            elevation = 8.dp,
                            onClick = { onPlaylistClick(playlist) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(16.dp))
                                ) {
                                    ArtworkImage(
                                        artworkUri = playlist.artworkUri,
                                        contentDescription = playlist.name,
                                        modifier = Modifier.size(130.dp),
                                        cornerRadius = 16.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${playlist.songCount} Tracks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GlassTheme.ActiveDockIcon,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Favorites Horizontal Carousel
        if (favoriteSongs.isNotEmpty()) {
            item {
                SectionHeader(title = "Your Favorites", onSeeAll = onNavigateToLibrary)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favoriteSongs, key = { it.id }) { song ->
                        GlassCard(
                            modifier = Modifier
                                .width(150.dp)
                                .testTag("fav_card_${song.id}"),
                            shape = RoundedCornerShape(22.dp),
                            backgroundColor = Color.White.copy(alpha = 0.08f),
                            borderColor = GlassTheme.BorderGlass,
                            elevation = 8.dp,
                            onClick = { onSongClick(song, favoriteSongs) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(16.dp))
                                ) {
                                    ArtworkImage(
                                        artworkUri = song.artworkUri,
                                        contentDescription = null,
                                        modifier = Modifier.size(130.dp),
                                        cornerRadius = 16.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.70f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Featured Albums Carousel
        if (albums.isNotEmpty()) {
            item {
                SectionHeader(title = "Featured Albums", onSeeAll = onNavigateToLibrary)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
            }
        }

        // Popular / Recent Tracks with Staggered Animations
        val popularTracks = if (allSongs.isNotEmpty()) allSongs.take(8) else recommendedSongs
        if (popularTracks.isNotEmpty()) {
            item {
                SectionHeader(title = "Tracks & Releases", onSeeAll = onNavigateToLibrary)
            }
            itemsIndexed(popularTracks.take(6), key = { index, item -> "pop_${item.id}" }) { index, song ->
                SongListItem(
                    song = song,
                    isCurrentSong = song.id == currentSong?.id,
                    isPlaying = isPlaying && song.id == currentSong?.id,
                    onClick = { onSongClick(song, popularTracks) },
                    onToggleFavorite = onToggleFavorite,
                    onAddToPlaylist = onAddToPlaylist,
                    onAddToQueue = onAddToQueue,
                    index = index
                )
            }
        }

        // Floating Bottom Glass Search Bar (Positioned near bottom, opens Search Overlay)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("bottom_search_trigger_bar"),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.50f),
                borderColor = GlassTheme.BorderGlassHighlight,
                elevation = 12.dp,
                onClick = onOpenSearchOverlay
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = GlassTheme.ActiveDockIcon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search tracks, artists, YouTube Music...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Discover",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassTheme.ActiveDockIcon,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        TextButton(onClick = onSeeAll) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelMedium,
                color = GlassTheme.ActiveDockIcon,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
