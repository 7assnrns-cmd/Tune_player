package com.example.ui.screens.settings.sections

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PRESET_ACCENTS = listOf(
    Color(0xFF7C4DFF) to "Electric Indigo",
    Color(0xFF00E676) to "Emerald Neon",
    Color(0xFFFF6D00) to "Sunset Amber",
    Color(0xFF00E5FF) to "Electric Cyan",
    Color(0xFFFF4081) to "Rose Quartz",
    Color(0xFFFFD600) to "Solar Gold"
)

@Composable
fun AppearanceSettingsSection(
    themeMode: String,
    dynamicColor: Boolean,
    accentColorIndex: Int,
    backgroundStyle: String,
    cornerRadiusDp: Int,
    reducedMotion: Boolean,
    playerLayout: String,
    onSetThemeMode: (String) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetAccentColorIndex: (Int) -> Unit,
    onSetBackgroundStyle: (String) -> Unit,
    onSetCornerRadiusDp: (Int) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onSetPlayerLayout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAccent = PRESET_ACCENTS.getOrNull(accentColorIndex)?.first ?: PRESET_ACCENTS[0].first

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("appearance_settings_section"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Live Preview Card
        Card(
            shape = RoundedCornerShape(cornerRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Live Theme Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = currentAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape((cornerRadiusDp / 2).dp))
                            .background(currentAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ArchiveTune Soundwave",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Theme: $themeMode • Radius: ${cornerRadiusDp}dp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Theme Mode Selector
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Theme Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("DARK" to "Dark Mode", "LIGHT" to "Light Mode", "SYSTEM" to "System").forEach { (mode, label) ->
                        val isSelected = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) currentAccent else MaterialTheme.colorScheme.surface)
                                .clickable { onSetThemeMode(mode) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Material You Dynamic Color", fontWeight = FontWeight.Medium)
                        Text(
                            text = "Extract dynamic tones from system wallpaper",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = dynamicColor, onCheckedChange = onSetDynamicColor)
                }
            }
        }

        // Accent Color Palette
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Accent Color Palette", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Applied to active highlights, buttons, and lyric glow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PRESET_ACCENTS.forEachIndexed { index, (color, name) ->
                        val isSelected = accentColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onSetAccentColorIndex(index) }
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Corner Radius & Blur
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Component Corner Radius: ${cornerRadiusDp}dp",
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = cornerRadiusDp.toFloat(),
                    onValueChange = { onSetCornerRadiusDp(it.toInt()) },
                    valueRange = 8f..28f,
                    steps = 4
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Reduced Motion", fontWeight = FontWeight.Medium)
                        Text(
                            text = "Disable intensive background animations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = reducedMotion, onCheckedChange = onSetReducedMotion)
                }
            }
        }
    }
}
