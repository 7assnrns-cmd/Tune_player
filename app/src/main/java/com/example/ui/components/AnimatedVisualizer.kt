package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassTheme
import kotlin.math.sin

/**
 * Real-time Translucent Frosted Glass Audio Spectrum Visualizer
 * Reacts smoothly to playback amplitude and rhythm
 */
@Composable
fun RealTimeAudioVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTheme.ActiveDockIcon,
    secondaryColor: Color = Color(0xFFA855F7)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_wave")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val bandCount = 16
    val currentAmplitudes = if (amplitudes.size >= bandCount) {
        amplitudes.take(bandCount)
    } else {
        List(bandCount) { 0.1f }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barSpacing = 4.dp.toPx()
            val availableWidth = totalWidth - (barSpacing * (bandCount - 1))
            val barWidth = (availableWidth / bandCount).coerceAtLeast(2.dp.toPx())

            for (i in 0 until bandCount) {
                val baseAmp = currentAmplitudes.getOrElse(i) { 0.1f }
                val dynamicBounce = if (isPlaying) {
                    val sinFactor = (sin(waveAnim + (i * 0.4f)) + 1f) / 2f
                    (baseAmp * 0.75f + sinFactor * 0.25f).coerceIn(0.12f, 1.0f)
                } else {
                    0.08f
                }

                val barHeight = (totalHeight * dynamicBounce).coerceAtLeast(4.dp.toPx())
                val x = i * (barWidth + barSpacing)
                val y = (totalHeight - barHeight) / 2f

                // Translucent neon gradient
                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.9f),
                        secondaryColor.copy(alpha = 0.85f),
                        accentColor.copy(alpha = 0.5f)
                    ),
                    startY = y,
                    endY = y + barHeight
                )

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
