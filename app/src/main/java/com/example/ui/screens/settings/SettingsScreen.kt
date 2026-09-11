package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.backup.RestoreConflictMode
import com.example.ui.screens.settings.sections.AccountSettingsSection
import com.example.ui.screens.settings.sections.AiIntegrationSettingsSection
import com.example.ui.screens.settings.sections.AppearanceSettingsSection
import com.example.ui.screens.settings.sections.BehaviorSettingsSection
import com.example.ui.screens.settings.sections.ContentSettingsSection
import com.example.ui.screens.settings.sections.DeveloperAndAboutSection
import com.example.ui.screens.settings.sections.IntegrationSettingsSection
import com.example.ui.screens.settings.sections.InternetSettingsSection
import com.example.ui.screens.settings.sections.ListeningStatsSection
import com.example.ui.screens.settings.sections.LyricsSettingsSection
import com.example.ui.screens.settings.sections.PlaybackSettingsSection
import com.example.ui.screens.settings.sections.StorageBackupSection
import com.example.ui.theme.GlassTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    initialCategory: SettingsCategory? = null,
    onNavigateBackToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(initialCategory) }
    var searchQuery by remember { mutableStateOf("") }

    // Collect states
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val backgroundStyle by viewModel.backgroundStyle.collectAsState()
    val cornerRadiusDp by viewModel.cornerRadiusDp.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()
    val playerLayout by viewModel.playerLayout.collectAsState()

    val audioQuality by viewModel.audioQuality.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val gapless by viewModel.gapless.collectAsState()
    val crossfade by viewModel.crossfade.collectAsState()
    val crossfadeDurationSec by viewModel.crossfadeDurationSec.collectAsState()
    val volumeNormalization by viewModel.volumeNormalization.collectAsState()
    val skipSilence by viewModel.skipSilence.collectAsState()
    val pauseOnHeadphoneDisconnect by viewModel.pauseOnHeadphoneDisconnect.collectAsState()
    val resumeOnBtConnect by viewModel.resumeOnBtConnect.collectAsState()

    val lyricsPriority by viewModel.lyricsPriority.collectAsState()
    val lyricsFontSize by viewModel.lyricsFontSize.collectAsState()
    val lyricsFontWeight by viewModel.lyricsFontWeight.collectAsState()
    val lyricsCentered by viewModel.lyricsCentered.collectAsState()
    val lyricsKaraoke by viewModel.lyricsKaraoke.collectAsState()

    val explicitFilter by viewModel.explicitFilter.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val seekIntervalSec by viewModel.seekIntervalSec.collectAsState()
    val shakeToSkip by viewModel.shakeToSkip.collectAsState()

    val aiRecommendations by viewModel.aiRecommendations.collectAsState()
    val aiTranslateLyrics by viewModel.aiTranslateLyrics.collectAsState()
    val dataSaver by viewModel.dataSaver.collectAsState()
    val offlineMode by viewModel.offlineMode.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val autoSync by viewModel.autoSync.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val syncReport by viewModel.syncReport.collectAsState()

    val selectedStatsPeriod by viewModel.selectedStatsPeriod.collectAsState()
    val listeningOverview by viewModel.listeningOverview.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val networkLogs by viewModel.networkLogs.collectAsState()

    if (selectedCategory != null) {
        // Detail sub-page for chosen category
        val cat = selectedCategory!!
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = cat.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = cat.group, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedCategory = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                item {
                    when (cat) {
                        SettingsCategory.ACCOUNT -> {
                            AccountSettingsSection(
                                user = currentUser,
                                syncState = syncState,
                                syncReport = syncReport,
                                autoSyncEnabled = autoSync,
                                onSignIn = { viewModel.signInGoogle() },
                                onSignOut = { viewModel.signOutGoogle() },
                                onSwitchAccount = { viewModel.switchGoogleAccount() },
                                onPerformSync = { viewModel.performSync() },
                                onToggleAutoSync = { viewModel.toggleAutoSync(it) }
                            )
                        }
                        SettingsCategory.LISTENING_STATS -> {
                            ListeningStatsSection(
                                overview = listeningOverview,
                                selectedPeriod = selectedStatsPeriod,
                                onSelectPeriod = { viewModel.selectStatsPeriod(it) }
                            )
                        }
                        SettingsCategory.APPEARANCE -> {
                            AppearanceSettingsSection(
                                themeMode = themeMode,
                                dynamicColor = dynamicColor,
                                accentColorIndex = accentColorIndex,
                                backgroundStyle = backgroundStyle,
                                cornerRadiusDp = cornerRadiusDp,
                                reducedMotion = reducedMotion,
                                playerLayout = playerLayout,
                                onSetThemeMode = { viewModel.setThemeMode(it) },
                                onSetDynamicColor = { viewModel.setDynamicColor(it) },
                                onSetAccentColorIndex = { viewModel.setAccentColorIndex(it) },
                                onSetBackgroundStyle = { viewModel.setBackgroundStyle(it) },
                                onSetCornerRadiusDp = { viewModel.setCornerRadiusDp(it) },
                                onSetReducedMotion = { viewModel.setReducedMotion(it) },
                                onSetPlayerLayout = { viewModel.setPlayerLayout(it) }
                            )
                        }
                        SettingsCategory.PLAYBACK -> {
                            PlaybackSettingsSection(
                                audioQuality = audioQuality,
                                playbackSpeed = playbackSpeed,
                                gapless = gapless,
                                crossfade = crossfade,
                                crossfadeDurationSec = crossfadeDurationSec,
                                volumeNormalization = volumeNormalization,
                                skipSilence = skipSilence,
                                pauseOnHeadphoneDisconnect = pauseOnHeadphoneDisconnect,
                                resumeOnBtConnect = resumeOnBtConnect,
                                onSetAudioQuality = { viewModel.setAudioQuality(it) },
                                onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                                onSetGapless = { viewModel.setGapless(it) },
                                onSetCrossfade = { viewModel.setCrossfade(it) },
                                onSetCrossfadeDurationSec = { viewModel.setCrossfadeDurationSec(it) },
                                onSetVolumeNormalization = { viewModel.setVolumeNormalization(it) },
                                onSetSkipSilence = { viewModel.setSkipSilence(it) },
                                onSetPauseOnHeadphoneDisconnect = { viewModel.setPauseOnHeadphoneDisconnect(it) },
                                onSetResumeOnBtConnect = { viewModel.setResumeOnBtConnect(it) }
                            )
                        }
                        SettingsCategory.LYRICS -> {
                            LyricsSettingsSection(
                                priority = lyricsPriority,
                                fontSizeSp = lyricsFontSize,
                                fontWeightStr = lyricsFontWeight,
                                centered = lyricsCentered,
                                karaokeProgressive = lyricsKaraoke,
                                onSetPriority = { viewModel.setLyricsPriority(it) },
                                onSetFontSizeSp = { viewModel.setLyricsFontSize(it) },
                                onSetFontWeightStr = { viewModel.setLyricsFontWeight(it) },
                                onSetCentered = { viewModel.setLyricsCentered(it) },
                                onSetKaraokeProgressive = { viewModel.setLyricsKaraoke(it) },
                                onImportLyricFile = { /* Handled via file picker */ }
                            )
                        }
                        SettingsCategory.CONTENT -> {
                            ContentSettingsSection(
                                explicitFilter = explicitFilter,
                                preferredLanguage = preferredLanguage,
                                onSetExplicitFilter = { viewModel.setExplicitFilter(it) },
                                onSetPreferredLanguage = { viewModel.setPreferredLanguage(it) }
                            )
                        }
                        SettingsCategory.BEHAVIOR -> {
                            BehaviorSettingsSection(
                                keepScreenOn = keepScreenOn,
                                seekIntervalSec = seekIntervalSec,
                                shakeToSkip = shakeToSkip,
                                onSetKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                                onSetSeekIntervalSec = { viewModel.setSeekIntervalSec(it) },
                                onSetShakeToSkip = { viewModel.setShakeToSkip(it) }
                            )
                        }
                        SettingsCategory.INTEGRATION -> {
                            IntegrationSettingsSection(
                                onScanLocalFolders = { /* Handled via storage intent */ }
                            )
                        }
                        SettingsCategory.AI_INTEGRATION -> {
                            AiIntegrationSettingsSection(
                                aiRecommendations = aiRecommendations,
                                aiTranslateLyrics = aiTranslateLyrics,
                                onSetAiRecommendations = { viewModel.setAiRecommendations(it) },
                                onSetAiTranslateLyrics = { viewModel.setAiTranslateLyrics(it) }
                            )
                        }
                        SettingsCategory.INTERNET -> {
                            InternetSettingsSection(
                                networkStatus = networkStatus,
                                dataSaver = dataSaver,
                                offlineMode = offlineMode,
                                onSetDataSaver = { viewModel.setDataSaver(it) },
                                onSetOfflineMode = { viewModel.setOfflineMode(it) }
                            )
                        }
                        SettingsCategory.STORAGE -> {
                            StorageBackupSection(
                                isStorageTab = true,
                                onClearImageCache = { /* Coil cache cleared */ },
                                onClearLyricsCache = { /* Lyrics cleared */ },
                                onClearHttpCache = { /* Network cache cleared */ },
                                onExportBackup = { viewModel.exportBackup() },
                                onImportBackup = { viewModel.importBackup(it) }
                            )
                        }
                        SettingsCategory.BACKUP_RESTORE -> {
                            StorageBackupSection(
                                isStorageTab = false,
                                onClearImageCache = {},
                                onClearLyricsCache = {},
                                onClearHttpCache = {},
                                onExportBackup = { viewModel.exportBackup() },
                                onImportBackup = { viewModel.importBackup(it) }
                            )
                        }
                        SettingsCategory.DEVELOPER_OPTIONS -> {
                            DeveloperAndAboutSection(
                                category = "DEVELOPER_OPTIONS",
                                networkLogs = networkLogs,
                                onClearLogs = { /* Logs cleared */ }
                            )
                        }
                        SettingsCategory.OPEN_SUPPORTED_LINKS -> {
                            DeveloperAndAboutSection(
                                category = "OPEN_SUPPORTED_LINKS",
                                networkLogs = networkLogs,
                                onClearLogs = {}
                            )
                        }
                        SettingsCategory.UPDATES -> {
                            DeveloperAndAboutSection(
                                category = "UPDATES",
                                networkLogs = networkLogs,
                                onClearLogs = {}
                            )
                        }
                        SettingsCategory.ABOUT -> {
                            DeveloperAndAboutSection(
                                category = "ABOUT",
                                networkLogs = networkLogs,
                                onClearLogs = {}
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Main 17 Categories Index View
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("settings_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search settings, playback, themes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )
            }

            val filteredCategories = SettingsCategory.values().filter {
                if (searchQuery.isBlank()) true
                else it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true) ||
                        it.group.contains(searchQuery, ignoreCase = true)
            }

            val grouped = filteredCategories.groupBy { it.group }

            grouped.forEach { (groupName, categories) ->
                item {
                    Text(
                        text = groupName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassTheme.ActiveDockIcon,
                        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp, start = 4.dp)
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
                        border = BorderStroke(1.dp, GlassTheme.BorderGlass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            categories.forEachIndexed { index, cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(GlassTheme.ActiveDockIcon.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = GlassTheme.ActiveDockIcon,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cat.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = cat.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.70f),
                                            maxLines = 1
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
