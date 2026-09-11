package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.SleepTimerOption
import com.example.ui.theme.GlassTheme

/**
 * Circular Radial Glassmorphic Sleep Timer Dialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CircularSleepTimerDialog(
    currentRemainingMs: Long?,
    onSelectOption: (SleepTimerOption) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val progress = if (currentRemainingMs != null && currentRemainingMs > 0) {
        // Compute circular percentage assuming standard 60m max for radial display
        (currentRemainingMs.toFloat() / (60 * 60 * 1000L).toFloat()).coerceIn(0.05f, 1f)
    } else if (currentRemainingMs == -1L) {
        1.0f
    } else {
        0.0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "timer_radial_progress"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.BorderGlassHighlight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .testTag("circular_sleep_timer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            tint = GlassTheme.ActiveDockIcon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sleep Timer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Radial Countdown Display
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        // Track ring
                        drawArc(
                            color = Color.White.copy(alpha = 0.1f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress ring
                        if (animatedProgress > 0f) {
                            drawArc(
                                color = GlassTheme.ActiveDockIcon,
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when {
                                currentRemainingMs == null -> "Off"
                                currentRemainingMs == -1L -> "Track End"
                                else -> formatTime(currentRemainingMs)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (currentRemainingMs != null) GlassTheme.ActiveDockIcon else Color.White
                        )
                        Text(
                            text = if (currentRemainingMs != null) "Time Remaining" else "Select Preset",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Presets Flow Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SleepTimerOption.values().forEach { option ->
                        val isSelected = when (option) {
                            SleepTimerOption.END_OF_TRACK -> currentRemainingMs == -1L
                            else -> false
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GlassTheme.ActiveDockIcon else Color.White.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    if (isSelected) GlassTheme.ActiveDockIcon else GlassTheme.BorderGlass,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectOption(option)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (currentRemainingMs != null) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCancelTimer()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Turn Off Sleep Timer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
