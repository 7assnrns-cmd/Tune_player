package com.example.ui.screens.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AudioQuality

@Composable
fun PlaybackSettingsSection(
    audioQuality: String,
    playbackSpeed: Float,
    gapless: Boolean,
    crossfade: Boolean,
    crossfadeDurationSec: Int,
    volumeNormalization: Boolean,
    skipSilence: Boolean,
    pauseOnHeadphoneDisconnect: Boolean,
    resumeOnBtConnect: Boolean,
    onSetAudioQuality: (String) -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSetGapless: (Boolean) -> Unit,
    onSetCrossfade: (Boolean) -> Unit,
    onSetCrossfadeDurationSec: (Int) -> Unit,
    onSetVolumeNormalization: (Boolean) -> Unit,
    onSetSkipSilence: (Boolean) -> Unit,
    onSetPauseOnHeadphoneDisconnect: (Boolean) -> Unit,
    onSetResumeOnBtConnect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("playback_settings_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Audio Quality Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Streaming Audio Quality", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioQuality.values().forEach { q ->
                        val isSelected = audioQuality == q.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetAudioQuality(q.name) },
                            label = { Text(q.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Transitions: Crossfade & Gapless
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Track Transitions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Gapless Playback", fontWeight = FontWeight.Medium)
                        Text("Seamlessly join consecutive tracks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = gapless, onCheckedChange = onSetGapless)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Crossfade", fontWeight = FontWeight.Medium)
                        Text("Smoothly overlap tracks: ${crossfadeDurationSec}s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = crossfade, onCheckedChange = onSetCrossfade)
                }

                if (crossfade) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = crossfadeDurationSec.toFloat(),
                        onValueChange = { onSetCrossfadeDurationSec(it.toInt()) },
                        valueRange = 1f..12f,
                        steps = 10
                    )
                }
            }
        }

        // Processing & Normalization
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Audio Processing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Volume Normalization", fontWeight = FontWeight.Medium)
                        Text("Balance volume level across disparate audio files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = volumeNormalization, onCheckedChange = onSetVolumeNormalization)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Skip Silence", fontWeight = FontWeight.Medium)
                        Text("Automatically skip dead gaps at track intro/outro", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = skipSilence, onCheckedChange = onSetSkipSilence)
                }
            }
        }

        // Hardware & Bluetooth Behavior
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Headphones & Bluetooth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Pause on Disconnect", fontWeight = FontWeight.Medium)
                        Text("Pause when headphones or speaker unplugs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = pauseOnHeadphoneDisconnect, onCheckedChange = onSetPauseOnHeadphoneDisconnect)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Auto-Resume on Bluetooth", fontWeight = FontWeight.Medium)
                        Text("Resume when reconnecting to car or audio receiver", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = resumeOnBtConnect, onCheckedChange = onSetResumeOnBtConnect)
                }
            }
        }
    }
}
