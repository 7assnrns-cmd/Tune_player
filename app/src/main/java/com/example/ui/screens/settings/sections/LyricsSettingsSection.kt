package com.example.ui.screens.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LyricsSettingsSection(
    priority: String,
    fontSizeSp: Int,
    fontWeightStr: String,
    centered: Boolean,
    karaokeProgressive: Boolean,
    onSetPriority: (String) -> Unit,
    onSetFontSizeSp: (Int) -> Unit,
    onSetFontWeightStr: (String) -> Unit,
    onSetCentered: (Boolean) -> Unit,
    onSetKaraokeProgressive: (Boolean) -> Unit,
    onImportLyricFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weight = when (fontWeightStr) {
        "NORMAL" -> FontWeight.Normal
        "MEDIUM" -> FontWeight.Medium
        else -> FontWeight.Bold
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lyrics_settings_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Lyric Typography Preview
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Live Typography Preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Across the boundless sea of midnight",
                    fontSize = fontSizeSp.sp,
                    fontWeight = weight,
                    color = Color.White,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "(Echoes in the cosmos calling you)",
                    fontSize = (fontSizeSp - 4).sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Format Priority
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Lyric Format Priority", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Order of preference when resolving lyrics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AUTO" to "Auto", "TTML" to "TTML", "LRC" to "LRC", "EMBEDDED" to "Embedded").forEach { (fmt, label) ->
                        val isSelected = priority == fmt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetPriority(fmt) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Font Size & Weight
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Font Size: ${fontSizeSp}sp", fontWeight = FontWeight.Medium)
                Slider(
                    value = fontSizeSp.toFloat(),
                    onValueChange = { onSetFontSizeSp(it.toInt()) },
                    valueRange = 16f..36f,
                    steps = 9
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Font Weight", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("NORMAL" to "Regular", "MEDIUM" to "Medium", "BOLD" to "Bold").forEach { (w, lbl) ->
                        val isSelected = fontWeightStr == w
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetFontWeightStr(w) },
                            label = { Text(lbl, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Center Text Alignment", fontWeight = FontWeight.Medium)
                        Text("Center lyrics instead of left alignment", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = centered, onCheckedChange = onSetCentered)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Progressive Karaoke Highlight", fontWeight = FontWeight.Medium)
                        Text("Word-by-word dynamic fill effect", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = karaokeProgressive, onCheckedChange = onSetKaraokeProgressive)
                }
            }
        }

        // Import Local Lyric File
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Import External Lyrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Load a .ttml or .lrc file from storage to sync with the current song",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onImportLyricFile,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select .ttml or .lrc File")
                }
            }
        }
    }
}
