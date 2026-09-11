package com.example.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.GoogleAccountManager
import com.example.account.GoogleUserProfile
import com.example.account.SyncEngine
import com.example.account.SyncReport
import com.example.account.SyncState
import com.example.backup.BackupManager
import com.example.backup.RestoreConflictMode
import com.example.data.local.DataStoreManager
import com.example.data.local.MusicDatabase
import com.example.data.mediastore.MediaStoreScanner
import com.example.data.repository.MusicRepositoryImpl
import com.example.network.NetworkLogEntry
import com.example.network.NetworkLogger
import com.example.network.NetworkMonitor
import com.example.network.NetworkStatus
import com.example.stats.ListeningOverview
import com.example.stats.StatsManager
import com.example.stats.StatsTimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = DataStoreManager(application)
    private val database = MusicDatabase.getInstance(application)
    private val repository = MusicRepositoryImpl(
        database.songDao(),
        database.playlistDao(),
        database.historyDao(),
        database.searchHistoryDao(),
        MediaStoreScanner(application)
    )

    val accountManager = GoogleAccountManager(application)
    val syncEngine = SyncEngine(accountManager, repository)
    val statsManager = StatsManager(database.historyDao(), database.songDao())
    val backupManager = BackupManager(application, database.songDao(), database.playlistDao())
    val networkMonitor = NetworkMonitor(application)

    // DataStore states
    val themeMode = dataStore.themeMode.stateIn(viewModelScope, SharingStarted.Lazily, "DARK")
    val dynamicColor = dataStore.dynamicColor.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val accentColorIndex = dataStore.accentColorIndex.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val backgroundStyle = dataStore.backgroundStyle.stateIn(viewModelScope, SharingStarted.Lazily, "OLED_BLACK")
    val cornerRadiusDp = dataStore.cornerRadiusDp.stateIn(viewModelScope, SharingStarted.Lazily, 16)
    val reducedMotion = dataStore.reducedMotion.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val playerLayout = dataStore.playerLayout.stateIn(viewModelScope, SharingStarted.Lazily, "MODERN")

    val audioQuality = dataStore.audioQuality.stateIn(viewModelScope, SharingStarted.Lazily, "HIGH")
    val playbackSpeed = dataStore.playbackSpeed.stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)
    val gapless = dataStore.gapless.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val crossfade = dataStore.crossfade.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val crossfadeDurationSec = dataStore.crossfadeDurationSec.stateIn(viewModelScope, SharingStarted.Lazily, 4)
    val volumeNormalization = dataStore.volumeNormalization.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val skipSilence = dataStore.skipSilence.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val pauseOnHeadphoneDisconnect = dataStore.pauseOnHeadphoneDisconnect.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val resumeOnBtConnect = dataStore.resumeOnBtConnect.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val lyricsPriority = dataStore.lyricsPriority.stateIn(viewModelScope, SharingStarted.Lazily, "AUTO")
    val lyricsFontSize = dataStore.lyricsFontSize.stateIn(viewModelScope, SharingStarted.Lazily, 24)
    val lyricsFontWeight = dataStore.lyricsFontWeight.stateIn(viewModelScope, SharingStarted.Lazily, "BOLD")
    val lyricsCentered = dataStore.lyricsCentered.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val lyricsKaraoke = dataStore.lyricsKaraoke.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val explicitFilter = dataStore.explicitFilter.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val preferredLanguage = dataStore.preferredLanguage.stateIn(viewModelScope, SharingStarted.Lazily, "All")
    val keepScreenOn = dataStore.keepScreenOn.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val seekIntervalSec = dataStore.seekIntervalSec.stateIn(viewModelScope, SharingStarted.Lazily, 10)
    val shakeToSkip = dataStore.shakeToSkip.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val aiRecommendations = dataStore.aiRecommendations.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val aiTranslateLyrics = dataStore.aiTranslateLyrics.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val dataSaver = dataStore.dataSaver.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val offlineMode = dataStore.offlineMode.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Account & Sync states
    val currentUser: StateFlow<GoogleUserProfile?> = accountManager.currentUser
    val autoSync: StateFlow<Boolean> = accountManager.autoSyncEnabled
    val syncState: StateFlow<SyncState> = syncEngine.syncState
    val syncReport: StateFlow<SyncReport> = syncEngine.lastReport

    // Listening Stats states
    private val _selectedStatsPeriod = MutableStateFlow(StatsTimePeriod.ALL_TIME)
    val selectedStatsPeriod: StateFlow<StatsTimePeriod> = _selectedStatsPeriod.asStateFlow()

    private val _listeningOverview = MutableStateFlow(ListeningOverview())
    val listeningOverview: StateFlow<ListeningOverview> = _listeningOverview.asStateFlow()

    // Network & Diagnostics
    val networkStatus: StateFlow<NetworkStatus> = networkMonitor.networkStatus.stateIn(viewModelScope, SharingStarted.Lazily, NetworkStatus())
    val networkLogs: StateFlow<List<NetworkLogEntry>> = NetworkLogger.logs

    init {
        loadStats(StatsTimePeriod.ALL_TIME)
    }

    fun selectStatsPeriod(period: StatsTimePeriod) {
        _selectedStatsPeriod.value = period
        loadStats(period)
    }

    private fun loadStats(period: StatsTimePeriod) {
        viewModelScope.launch {
            _listeningOverview.value = statsManager.getOverviewForPeriod(period)
        }
    }

    // Setters
    fun setThemeMode(mode: String) = viewModelScope.launch { dataStore.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { dataStore.setDynamicColor(enabled) }
    fun setAccentColorIndex(index: Int) = viewModelScope.launch { dataStore.setAccentColorIndex(index) }
    fun setBackgroundStyle(style: String) = viewModelScope.launch { dataStore.setBackgroundStyle(style) }
    fun setCornerRadiusDp(radius: Int) = viewModelScope.launch { dataStore.setCornerRadiusDp(radius) }
    fun setReducedMotion(reduced: Boolean) = viewModelScope.launch { dataStore.setReducedMotion(reduced) }
    fun setPlayerLayout(layout: String) = viewModelScope.launch { dataStore.setPlayerLayout(layout) }

    fun setAudioQuality(q: String) = viewModelScope.launch { dataStore.setAudioQuality(q) }
    fun setPlaybackSpeed(s: Float) = viewModelScope.launch { dataStore.setPlaybackSpeed(s) }
    fun setGapless(g: Boolean) = viewModelScope.launch { dataStore.setGapless(g) }
    fun setCrossfade(c: Boolean) = viewModelScope.launch { dataStore.setCrossfade(c) }
    fun setCrossfadeDurationSec(sec: Int) = viewModelScope.launch { dataStore.setCrossfadeDurationSec(sec) }
    fun setVolumeNormalization(v: Boolean) = viewModelScope.launch { dataStore.setVolumeNormalization(v) }
    fun setSkipSilence(s: Boolean) = viewModelScope.launch { dataStore.setSkipSilence(s) }
    fun setPauseOnHeadphoneDisconnect(p: Boolean) = viewModelScope.launch { dataStore.setPauseOnHeadphoneDisconnect(p) }
    fun setResumeOnBtConnect(r: Boolean) = viewModelScope.launch { dataStore.setResumeOnBtConnect(r) }

    fun setLyricsPriority(p: String) = viewModelScope.launch { dataStore.setLyricsPriority(p) }
    fun setLyricsFontSize(sz: Int) = viewModelScope.launch { dataStore.setLyricsFontSize(sz) }
    fun setLyricsFontWeight(w: String) = viewModelScope.launch { dataStore.setLyricsFontWeight(w) }
    fun setLyricsCentered(c: Boolean) = viewModelScope.launch { dataStore.setLyricsCentered(c) }
    fun setLyricsKaraoke(k: Boolean) = viewModelScope.launch { dataStore.setLyricsKaraoke(k) }

    fun setExplicitFilter(f: Boolean) = viewModelScope.launch { dataStore.setExplicitFilter(f) }
    fun setPreferredLanguage(l: String) = viewModelScope.launch { dataStore.setPreferredLanguage(l) }
    fun setKeepScreenOn(k: Boolean) = viewModelScope.launch { dataStore.setKeepScreenOn(k) }
    fun setSeekIntervalSec(s: Int) = viewModelScope.launch { dataStore.setSeekIntervalSec(s) }
    fun setShakeToSkip(s: Boolean) = viewModelScope.launch { dataStore.setShakeToSkip(s) }

    fun setAiRecommendations(r: Boolean) = viewModelScope.launch { dataStore.setAiRecommendations(r) }
    fun setAiTranslateLyrics(t: Boolean) = viewModelScope.launch { dataStore.setAiTranslateLyrics(t) }
    fun setDataSaver(d: Boolean) = viewModelScope.launch { dataStore.setDataSaver(d) }
    fun setOfflineMode(o: Boolean) = viewModelScope.launch { dataStore.setOfflineMode(o) }

    // Account Actions
    fun signInGoogle() = viewModelScope.launch { accountManager.signInWithGoogle() }
    fun signOutGoogle() = viewModelScope.launch { accountManager.signOut() }
    fun switchGoogleAccount() = viewModelScope.launch { accountManager.switchAccount("music.creator@gmail.com", "Studio Creator") }
    fun performSync() = viewModelScope.launch { syncEngine.performSync() }
    fun toggleAutoSync(enabled: Boolean) = accountManager.setAutoSyncEnabled(enabled)

    // Storage & Backup
    fun clearHistory() = viewModelScope.launch { repository.clearPlaybackHistory() }
    fun clearSearches() = viewModelScope.launch { database.searchHistoryDao().clearSearches() }
    fun exportBackup() = viewModelScope.launch { backupManager.createBackupJson() }
    fun importBackup(mode: RestoreConflictMode) = viewModelScope.launch {
        val json = backupManager.createBackupJson()
        backupManager.restoreFromJson(json, mode)
    }
}
