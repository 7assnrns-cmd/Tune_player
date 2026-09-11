package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Song
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.glassEffect
import kotlinx.coroutines.delay

@Composable
fun SongListItem(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Staggered animated appearance
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 35L).coerceAtMost(300L))
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ),
        modifier = modifier
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("song_item_${song.id}"),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = if (isCurrentSong) {
                GlassTheme.ActiveDockIcon.copy(alpha = 0.16f)
            } else {
                Color.White.copy(alpha = 0.05f)
            },
            borderColor = if (isCurrentSong) {
                GlassTheme.ActiveDockIcon.copy(alpha = 0.45f)
            } else {
                GlassTheme.BorderGlass
            },
            borderWidth = 1.dp,
            elevation = if (isCurrentSong) 8.dp else 2.dp,
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ArtworkImage(
                        artworkUri = song.artworkUri,
                        contentDescription = "${song.title} artwork",
                        modifier = Modifier.size(52.dp),
                        cornerRadius = 14.dp
                    )
                    if (isCurrentSong) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            VisualizerWave(
                                isPlaying = isPlaying,
                                modifier = Modifier.padding(4.dp),
                                barColor = GlassTheme.ActiveDockIcon,
                                maxHeight = 22.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrentSong) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isCurrentSong) GlassTheme.ActiveDockIcon else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.70f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = " • ${formatTime(song.durationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.50f)
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite(song) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("fav_btn_${song.id}")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (song.isFavorite) Color(0xFFF43F5E) else Color.White.copy(alpha = 0.60f)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("more_btn_${song.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More actions for ${song.title}",
                            tint = Color.White.copy(alpha = 0.70f)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .glassEffect(
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.90f),
                                borderColor = GlassTheme.BorderGlassHighlight
                            )
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to playlist", color = Color.White) },
                            leadingIcon = {
                                Icon(Icons.Outlined.PlaylistAdd, contentDescription = null, tint = GlassTheme.ActiveDockIcon)
                            },
                            onClick = {
                                showMenu = false
                                onAddToPlaylist(song)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to queue", color = Color.White) },
                            leadingIcon = {
                                Icon(Icons.Outlined.QueueMusic, contentDescription = null, tint = GlassTheme.ActiveDockIcon)
                            },
                            onClick = {
                                showMenu = false
                                onAddToQueue(song)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .width(160.dp)
            .testTag("album_card_${album.id}"),
        shape = RoundedCornerShape(GlassTheme.CornerRadiusCard),
        backgroundColor = Color.White.copy(alpha = 0.08f),
        borderColor = GlassTheme.BorderGlass,
        elevation = 8.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(18.dp))
            ) {
                ArtworkImage(
                    artworkUri = album.artworkUri,
                    contentDescription = "${album.title} cover",
                    modifier = Modifier.size(140.dp),
                    cornerRadius = 18.dp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = album.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${album.songCount} songs",
                style = MaterialTheme.typography.labelSmall,
                color = GlassTheme.ActiveDockIcon,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .width(124.dp)
            .testTag("artist_card_${artist.id}"),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color.White.copy(alpha = 0.07f),
        borderColor = GlassTheme.BorderGlass,
        elevation = 6.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, GlassTheme.ActiveDockIcon.copy(alpha = 0.5f), CircleShape)
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ArtworkImage(
                    artworkUri = null,
                    contentDescription = "${artist.name} photo",
                    modifier = Modifier.size(80.dp),
                    cornerRadius = 40.dp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${artist.songCount} tracks",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.60f)
            )
        }
    }
}
