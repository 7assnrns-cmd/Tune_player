package com.example.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val group: String
) {
    ACCOUNT("Account & Sync", "Google Sign-In, profile & two-way cloud sync", Icons.Filled.AccountCircle, "Profile & Social"),
    LISTENING_STATS("Listening Stats", "Streaks, total hours, top artists & local metrics", Icons.Filled.BarChart, "Profile & Social"),

    APPEARANCE("Appearance", "Themes, 6 accent colors, blur & live card preview", Icons.Filled.ColorLens, "Personalization"),
    LYRICS("Lyrics Engine", "TTML / LRC parser, karaoke progressive fill & typography", Icons.Filled.Lyrics, "Personalization"),

    PLAYBACK("Playback & Audio", "Audio quality, crossfade, gapless & volume normalize", Icons.Filled.PlayCircleFilled, "Audio & Engine"),
    CONTENT("Content & Language", "Explicit filter, lyrics language & metadata sources", Icons.Filled.Tune, "Audio & Engine"),
    BEHAVIOR("App Behavior", "Keep screen on, seek duration & shake-to-skip", Icons.Filled.SettingsSuggest, "Audio & Engine"),

    INTEGRATION("Integrations", "Last.fm scrobble, local folders & Android Auto", Icons.Filled.Devices, "Connectivity"),
    AI_INTEGRATION("AI Studio Integration", "Gemini smart playlist generator & lyric translations", Icons.Filled.AutoAwesome, "Connectivity"),
    INTERNET("Internet & Network", "Bandwidth monitor, metered data saver & cache policy", Icons.Filled.Wifi, "Connectivity"),

    STORAGE("Storage & Cache", "Coil image cache, TTML cache, Room DB & cleanup", Icons.Filled.Storage, "Data & System"),
    BACKUP_RESTORE("Backup & Restore", "Export JSON backup with checksum & import restore", Icons.Filled.Security, "Data & System"),
    DEVELOPER_OPTIONS("Developer Options", "Network logs, Media3 buffer health & TTML validator", Icons.Filled.GraphicEq, "Data & System"),

    OPEN_SUPPORTED_LINKS("Open Supported Links", "App links, YouTube Music URIs & file associations", Icons.Filled.Link, "System & Info"),
    UPDATES("Updates", "ArchiveTune release notes & update checker", Icons.Filled.SystemUpdate, "System & Info"),
    ABOUT("About ArchiveTune", "Native Android architecture, Media3 & licenses", Icons.Filled.Info, "System & Info")
}
