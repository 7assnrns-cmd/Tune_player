package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

/**
 * Premium Continuous Frosted Glass Playback Progress Bar with Smooth Seek Scrubbing
 */
@Composable
fun PlaybackProgressSlider(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF38BDF8),
    inactiveColor: Color = Color.White.copy(alpha = 0.16f)
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var dragRatio by remember { mutableFloatStateOf(0f) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    val actualRatio = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val effectiveRatio = if (isDragging) dragRatio else actualRatio
    val displayPosition = if (isDragging) {
        (dragRatio * durationMs).toLong()
    } else currentPositionMs

    val thumbSize by animateDpAsState(
        targetValue = if (isDragging) 18.dp else 14.dp,
        animationSpec = spring(),
        label = "thumb_size"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("playback_progress_container")
    ) {
        // Continuous Interactive Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val tappedRatio = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        onSeek((tappedRatio * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            dragRatio = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragRatio * durationMs).toLong())
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragRatio = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Background Inactive Track (Continuous Bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(inactiveColor)
            )

            // Active Progress Bar (Continuous Gradient Fill)
            Box(
                modifier = Modifier
                    .fillMaxWidth(effectiveRatio)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.8f),
                                activeColor,
                                Color(0xFF818CF8)
                            )
                        )
                    )
            )

            // Glowing Continuous Thumb
            val thumbHalfWidthPx = with(density) { (if (isDragging) 9.dp else 7.dp).toPx() }
            val thumbWidthPx = with(density) { (if (isDragging) 18.dp else 14.dp).toPx() }
            val thumbOffsetPx = (effectiveRatio * trackWidthPx - thumbHalfWidthPx)
                .coerceIn(0f, (trackWidthPx - thumbWidthPx).coerceAtLeast(0f))

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                    .size(thumbSize)
                    .shadow(6.dp, CircleShape, spotColor = activeColor)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        // Time Indicator Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(displayPosition),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
            Text(
                text = formatTime(durationMs),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.50f),
                fontSize = 11.sp
            )
        }
    }
}
