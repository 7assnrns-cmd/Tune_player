package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.domain.model.Song
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.glassEffect
import dev.chrisbanes.haze.HazeState

@Composable
fun MiniPlayer(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(),
        modifier = modifier
    ) {
        if (currentSong == null) return@AnimatedVisibility

        val progress = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(250),
            label = "mini_player_progress"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = GlassTheme.CardShape,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = GlassTheme.ActiveDockIcon.copy(alpha = 0.3f)
                )
                .glassEffect(
                    hazeState = hazeState,
                    shape = GlassTheme.CardShape,
                    backgroundColor = Color(0xFF0F172A).copy(alpha = 0.85f),
                    borderColor = GlassTheme.BorderGlassHighlight,
                    borderWidth = 1.dp,
                    elevation = 16.dp
                )
                .clickable(onClick = onClick)
                .testTag("mini_player")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Subtle top gradient progress line
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = GlassTheme.ActiveDockIcon,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork with frosted rounded corners
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(14.dp))
                    ) {
                        ArtworkImage(
                            artworkUri = currentSong.artworkUri,
                            contentDescription = "${currentSong.title} cover",
                            modifier = Modifier.size(48.dp),
                            cornerRadius = 14.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.70f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Play/Pause pill button
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GlassTheme.ActiveDockIcon.copy(alpha = 0.20f))
                            .border(1.dp, GlassTheme.ActiveDockIcon.copy(alpha = 0.40f), CircleShape)
                            .testTag("mini_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = GlassTheme.ActiveDockIcon,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Skip next button
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .testTag("mini_skip_next_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
