package com.example.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AudioQuality
import com.example.domain.model.PlaybackState
import com.example.domain.model.RepeatMode
import com.example.domain.model.SleepTimerOption
import com.example.domain.model.Song
import com.example.lyrics.LyricsManager
import com.example.lyrics.model.LyricDocument
import com.example.lyrics.renderer.AppleMusicLyricsView
import com.example.ui.components.ArtworkImage
import com.example.ui.components.AudioQualityDialog
import com.example.ui.components.CircularSleepTimerDialog
import com.example.ui.components.PlaybackProgressSlider
import com.example.ui.components.RealTimeAudioVisualizer
import com.example.ui.components.ShareLyricsImageDialog
import com.example.ui.components.formatTime
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.glassEffect
import com.example.ui.theme.rememberAnimatedDynamicPalette
import dev.chrisbanes.haze.HazeState

/**
 * Redesigned High-Fidelity Glassmorphic "Now Playing" Screen
 * 
 * Features:
 * - Dynamic color palette extraction with 700ms smooth background animation
 * - Subtle parallax / scale on album artwork during touch & playback
 * - Smooth morphing and bouncing Play/Pause button
 * - Translucent real-time animated audio spectrum visualizer
 * - Karaoke Mode toggle (vocal frequency attenuation)
 * - Share Lyrics as Frosted Image Card dialog
 * - Circular Radial Sleep Timer dialog
 * - Haptic feedback integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onFastForward: () -> Unit,
    onRewind: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSetAudioQuality: (AudioQuality) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onToggleKaraokeMode: (() -> Unit)? = null,
    onSelectSleepTimerOption: ((SleepTimerOption) -> Unit)? = null,
    onCancelSleepTimer: (() -> Unit)? = null
) {
    val currentSong = playbackState.currentSong ?: return
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Dynamic Color Extraction from Album Art
    val dynamicPalette = rememberAnimatedDynamicPalette(currentSong)

    // Lyrics State
    val lyricsManager = remember { LyricsManager(context) }
    var isLyricsMode by remember { mutableStateOf(false) }
    var lyricDocument by remember(currentSong.id) { mutableStateOf<LyricDocument?>(null) }
    var showShareLyricsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentSong.id) {
        lyricDocument = lyricsManager.getLyricsForSong(currentSong.id, currentSong.title, currentSong.artist)
    }

    var showSpeedMenu by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showCircularTimerDialog by remember { mutableStateOf(false) }

    // Parallax Artwork Drag / Touch interaction
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val animatedArtworkScale by animateFloatAsState(
        targetValue = if (playbackState.isPlaying) 1.0f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "artwork_scale"
    )

    // Play/Pause Button Press & Bounce Animation
    val playPauseInteractionSource = remember { MutableInteractionSource() }
    val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()
    val playPauseScale by animateFloatAsState(
        targetValue = if (isPlayPausePressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "play_pause_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_player_screen")
    ) {
        // Dynamic Animated Ambient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dynamicPalette.backgroundBrush)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("player_collapse_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isLyricsMode) "SYNCHRONIZED LYRICS" else "PLAYING FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isLyricsMode) currentSong.title else currentSong.album,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Lyrics Toggle Button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isLyricsMode = !isLyricsMode
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isLyricsMode) dynamicPalette.accent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                            .testTag("player_lyrics_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lyrics,
                            contentDescription = "Toggle Synchronized Lyrics",
                            tint = if (isLyricsMode) dynamicPalette.accent else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Queue Button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenQueue()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .testTag("player_queue_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = "View Queue",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Center Display: Synchronized Lyrics View OR Parallax Album Artwork
            if (isLyricsMode) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .glassEffect(
                            hazeState = hazeState,
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = Color.Black.copy(alpha = 0.35f),
                            borderColor = GlassTheme.BorderGlass
                        )
                        .padding(12.dp)
                ) {
                    AppleMusicLyricsView(
                        document = lyricDocument,
                        currentPositionMs = playbackState.currentPositionMs,
                        onSeekTo = onSeek,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Share lyrics floating chip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.BorderGlassHighlight),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clickable {
                                val currentLyric = lyricDocument?.lines?.find {
                                    playbackState.currentPositionMs in it.startTimeMs..it.endTimeMs
                                }?.text ?: currentSong.title
                                showShareLyricsDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = null,
                                tint = dynamicPalette.accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share Lyric",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = animatedArtworkScale
                            scaleY = animatedArtworkScale
                            translationX = dragOffsetX * 0.15f
                            translationY = dragOffsetY * 0.15f
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetX = (dragOffsetX + dragAmount.x).coerceIn(-40f, 40f)
                                    dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-40f, 40f)
                                },
                                onDragEnd = {
                                    dragOffsetX = 0f
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    dragOffsetX = 0f
                                    dragOffsetY = 0f
                                }
                            )
                        }
                        .shadow(32.dp, shape = RoundedCornerShape(28.dp), ambientColor = dynamicPalette.dominant, spotColor = dynamicPalette.accent)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, GlassTheme.BorderGlassHighlight, RoundedCornerShape(28.dp))
                ) {
                    ArtworkImage(
                        artworkUri = currentSong.artworkUri,
                        contentDescription = "${currentSong.title} large artwork",
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 28.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Frosted Glass Audio Spectrum Visualizer
            RealTimeAudioVisualizer(
                amplitudes = playbackState.visualizerAmplitudes,
                isPlaying = playbackState.isPlaying,
                accentColor = dynamicPalette.accent,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Song Info & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite(currentSong)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("player_fav_btn")
                ) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (currentSong.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (currentSong.isFavorite) Color(0xFFF43F5E) else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Chips Row: Audio Quality + Karaoke Mode Toggle + Sleep Timer Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quality badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.BorderGlass),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showQualityDialog = true
                        }
                        .testTag("quality_badge")
                ) {
                    Text(
                        text = playbackState.audioQuality.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = dynamicPalette.accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Karaoke Mode Toggle Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (playbackState.isKaraokeMode) dynamicPalette.accent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (playbackState.isKaraokeMode) dynamicPalette.accent else GlassTheme.BorderGlass
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleKaraokeMode?.invoke()
                        }
                        .testTag("karaoke_toggle_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (playbackState.isKaraokeMode) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (playbackState.isKaraokeMode) dynamicPalette.accent else Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (playbackState.isKaraokeMode) "Karaoke: ON" else "Karaoke",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (playbackState.isKaraokeMode) dynamicPalette.accent else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Sleep Timer Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (playbackState.sleepTimerRemainingMs != null) dynamicPalette.accent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.BorderGlass),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCircularTimerDialog = true
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (playbackState.sleepTimerRemainingMs != null) dynamicPalette.accent else Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (playbackState.sleepTimerRemainingMs != null) {
                                if (playbackState.sleepTimerRemainingMs == -1L) "End of track" else formatTime(playbackState.sleepTimerRemainingMs)
                            } else "Timer",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (playbackState.sleepTimerRemainingMs != null) dynamicPalette.accent else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Progress Slider
            PlaybackProgressSlider(
                currentPositionMs = playbackState.currentPositionMs,
                durationMs = playbackState.durationMs,
                onSeek = onSeek,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Primary Playback Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleShuffle()
                    },
                    modifier = Modifier.size(48.dp).testTag("shuffle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.shuffleMode) dynamicPalette.accent else Color.White.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRewind()
                    },
                    modifier = Modifier.size(48.dp).testTag("rewind_10_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipPrevious()
                    },
                    modifier = Modifier.size(48.dp).testTag("prev_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Morphing & Bouncing Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = playPauseScale
                            scaleY = playPauseScale
                        }
                        .shadow(16.dp, CircleShape, spotColor = dynamicPalette.accent)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    dynamicPalette.accent,
                                    Color(0xFF38BDF8)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = playPauseInteractionSource,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePlayPause()
                            }
                        )
                        .testTag("play_pause_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipNext()
                    },
                    modifier = Modifier.size(48.dp).testTag("next_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFastForward()
                    },
                    modifier = Modifier.size(48.dp).testTag("ff_30_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Forward30,
                        contentDescription = "Fast forward 30s",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCycleRepeatMode()
                    },
                    modifier = Modifier.size(48.dp).testTag("repeat_btn")
                ) {
                    Icon(
                        imageVector = if (playbackState.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat: ${playbackState.repeatMode.name}",
                        tint = if (playbackState.repeatMode != RepeatMode.OFF) dynamicPalette.accent else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Bottom Secondary Options (Speed & Timer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.BorderGlass),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showSpeedMenu = true
                            }
                            .testTag("speed_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Speed,
                                contentDescription = "Playback Speed",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${playbackState.playbackSpeed}x",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false }
                    ) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = {
                                    onSetPlaybackSpeed(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.BorderGlass),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCircularTimerDialog = true
                        }
                        .testTag("sleep_timer_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (playbackState.sleepTimerRemainingMs != null) dynamicPalette.accent else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Timer",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showQualityDialog) {
        AudioQualityDialog(
            currentQuality = playbackState.audioQuality,
            onSelectQuality = onSetAudioQuality,
            onDismiss = { showQualityDialog = false }
        )
    }

    if (showCircularTimerDialog) {
        CircularSleepTimerDialog(
            currentRemainingMs = playbackState.sleepTimerRemainingMs,
            onSelectOption = { option ->
                onSelectSleepTimerOption?.invoke(option)
                showCircularTimerDialog = false
            },
            onCancelTimer = {
                onCancelSleepTimer?.invoke()
                showCircularTimerDialog = false
            },
            onDismiss = { showCircularTimerDialog = false }
        )
    }

    if (showShareLyricsDialog) {
        val currentLyric = lyricDocument?.lines?.find {
            playbackState.currentPositionMs in it.startTimeMs..it.endTimeMs
        }?.text ?: "Cause you make me feel like I'm alive again..."

        ShareLyricsImageDialog(
            song = currentSong,
            lyricText = currentLyric,
            onDismiss = { showShareLyricsDialog = false }
        )
    }
}
