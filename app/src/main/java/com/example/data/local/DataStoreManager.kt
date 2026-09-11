package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.musicDataStore: DataStore<Preferences> by preferencesDataStore(name = "music_player_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        // Appearance
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "DARK", "LIGHT"
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_ACCENT_COLOR_INDEX = intPreferencesKey("accent_color_index")
        val KEY_BACKGROUND_STYLE = stringPreferencesKey("background_style")
        val KEY_CORNER_RADIUS_DP = intPreferencesKey("corner_radius_dp")
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val KEY_PLAYER_LAYOUT = stringPreferencesKey("player_layout") // "MODERN", "CLASSIC", "MINIMAL"

        // Playback
        val KEY_AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val KEY_PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val KEY_GAPLESS = booleanPreferencesKey("gapless_playback")
        val KEY_CROSSFADE = booleanPreferencesKey("crossfade")
        val KEY_CROSSFADE_DURATION_SEC = intPreferencesKey("crossfade_duration_sec")
        val KEY_VOLUME_NORMALIZATION = booleanPreferencesKey("volume_normalization")
        val KEY_SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val KEY_PAUSE_ON_HEADPHONE_DISCONNECT = booleanPreferencesKey("pause_on_headphone_disconnect")
        val KEY_RESUME_ON_BT_CONNECT = booleanPreferencesKey("resume_on_bt_connect")

        // Lyrics
        val KEY_LYRICS_PRIORITY = stringPreferencesKey("lyrics_priority") // "AUTO", "TTML", "LRC", "EMBEDDED"
        val KEY_LYRICS_FONT_SIZE = intPreferencesKey("lyrics_font_size")
        val KEY_LYRICS_FONT_WEIGHT = stringPreferencesKey("lyrics_font_weight") // "NORMAL", "MEDIUM", "BOLD"
        val KEY_LYRICS_CENTERED = booleanPreferencesKey("lyrics_centered")
        val KEY_LYRICS_KARAOKE = booleanPreferencesKey("lyrics_karaoke")

        // Content & Behavior
        val KEY_EXPLICIT_FILTER = booleanPreferencesKey("explicit_filter")
        val KEY_PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val KEY_SEEK_INTERVAL_SEC = intPreferencesKey("seek_interval_sec")
        val KEY_SHAKE_TO_SKIP = booleanPreferencesKey("shake_to_skip")

        // AI
        val KEY_AI_RECOMMENDATIONS = booleanPreferencesKey("ai_recommendations")
        val KEY_AI_TRANSLATE_LYRICS = booleanPreferencesKey("ai_translate_lyrics")

        // Network
        val KEY_DATA_SAVER = booleanPreferencesKey("data_saver")
        val KEY_OFFLINE_MODE = booleanPreferencesKey("offline_mode")

        // Library
        val KEY_LIBRARY_SORT = stringPreferencesKey("library_sort")
    }

    // Appearance flows
    val themeMode: Flow<String> = context.musicDataStore.data.map { it[KEY_THEME_MODE] ?: "DARK" }
    val dynamicColor: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_DYNAMIC_COLOR] ?: false }
    val accentColorIndex: Flow<Int> = context.musicDataStore.data.map { it[KEY_ACCENT_COLOR_INDEX] ?: 0 }
    val backgroundStyle: Flow<String> = context.musicDataStore.data.map { it[KEY_BACKGROUND_STYLE] ?: "OLED_BLACK" }
    val cornerRadiusDp: Flow<Int> = context.musicDataStore.data.map { it[KEY_CORNER_RADIUS_DP] ?: 16 }
    val reducedMotion: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_REDUCED_MOTION] ?: false }
    val playerLayout: Flow<String> = context.musicDataStore.data.map { it[KEY_PLAYER_LAYOUT] ?: "MODERN" }

    // Playback flows
    val audioQuality: Flow<String> = context.musicDataStore.data.map { it[KEY_AUDIO_QUALITY] ?: "HIGH" }
    val playbackSpeed: Flow<Float> = context.musicDataStore.data.map { it[KEY_PLAYBACK_SPEED] ?: 1.0f }
    val gapless: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_GAPLESS] ?: true }
    val crossfade: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_CROSSFADE] ?: false }
    val crossfadeDurationSec: Flow<Int> = context.musicDataStore.data.map { it[KEY_CROSSFADE_DURATION_SEC] ?: 4 }
    val volumeNormalization: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_VOLUME_NORMALIZATION] ?: true }
    val skipSilence: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_SKIP_SILENCE] ?: false }
    val pauseOnHeadphoneDisconnect: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_PAUSE_ON_HEADPHONE_DISCONNECT] ?: true }
    val resumeOnBtConnect: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_RESUME_ON_BT_CONNECT] ?: false }

    // Lyrics flows
    val lyricsPriority: Flow<String> = context.musicDataStore.data.map { it[KEY_LYRICS_PRIORITY] ?: "AUTO" }
    val lyricsFontSize: Flow<Int> = context.musicDataStore.data.map { it[KEY_LYRICS_FONT_SIZE] ?: 24 }
    val lyricsFontWeight: Flow<String> = context.musicDataStore.data.map { it[KEY_LYRICS_FONT_WEIGHT] ?: "BOLD" }
    val lyricsCentered: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_LYRICS_CENTERED] ?: false }
    val lyricsKaraoke: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_LYRICS_KARAOKE] ?: true }

    // Content & Behavior flows
    val explicitFilter: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_EXPLICIT_FILTER] ?: false }
    val preferredLanguage: Flow<String> = context.musicDataStore.data.map { it[KEY_PREFERRED_LANGUAGE] ?: "All" }
    val keepScreenOn: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_KEEP_SCREEN_ON] ?: false }
    val seekIntervalSec: Flow<Int> = context.musicDataStore.data.map { it[KEY_SEEK_INTERVAL_SEC] ?: 10 }
    val shakeToSkip: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_SHAKE_TO_SKIP] ?: false }

    // AI & Network flows
    val aiRecommendations: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_AI_RECOMMENDATIONS] ?: true }
    val aiTranslateLyrics: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_AI_TRANSLATE_LYRICS] ?: true }
    val dataSaver: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_DATA_SAVER] ?: false }
    val offlineMode: Flow<Boolean> = context.musicDataStore.data.map { it[KEY_OFFLINE_MODE] ?: false }
    val librarySort: Flow<String> = context.musicDataStore.data.map { it[KEY_LIBRARY_SORT] ?: "TITLE" }

    // Setters
    suspend fun setThemeMode(mode: String) = context.musicDataStore.edit { it[KEY_THEME_MODE] = mode }
    suspend fun setDynamicColor(enabled: Boolean) = context.musicDataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    suspend fun setAccentColorIndex(index: Int) = context.musicDataStore.edit { it[KEY_ACCENT_COLOR_INDEX] = index }
    suspend fun setBackgroundStyle(style: String) = context.musicDataStore.edit { it[KEY_BACKGROUND_STYLE] = style }
    suspend fun setCornerRadiusDp(radius: Int) = context.musicDataStore.edit { it[KEY_CORNER_RADIUS_DP] = radius }
    suspend fun setReducedMotion(reduced: Boolean) = context.musicDataStore.edit { it[KEY_REDUCED_MOTION] = reduced }
    suspend fun setPlayerLayout(layout: String) = context.musicDataStore.edit { it[KEY_PLAYER_LAYOUT] = layout }

    suspend fun setAudioQuality(quality: String) = context.musicDataStore.edit { it[KEY_AUDIO_QUALITY] = quality }
    suspend fun setPlaybackSpeed(speed: Float) = context.musicDataStore.edit { it[KEY_PLAYBACK_SPEED] = speed }
    suspend fun setGapless(enabled: Boolean) = context.musicDataStore.edit { it[KEY_GAPLESS] = enabled }
    suspend fun setCrossfade(enabled: Boolean) = context.musicDataStore.edit { it[KEY_CROSSFADE] = enabled }
    suspend fun setCrossfadeDurationSec(sec: Int) = context.musicDataStore.edit { it[KEY_CROSSFADE_DURATION_SEC] = sec }
    suspend fun setVolumeNormalization(enabled: Boolean) = context.musicDataStore.edit { it[KEY_VOLUME_NORMALIZATION] = enabled }
    suspend fun setSkipSilence(enabled: Boolean) = context.musicDataStore.edit { it[KEY_SKIP_SILENCE] = enabled }
    suspend fun setPauseOnHeadphoneDisconnect(enabled: Boolean) = context.musicDataStore.edit { it[KEY_PAUSE_ON_HEADPHONE_DISCONNECT] = enabled }
    suspend fun setResumeOnBtConnect(enabled: Boolean) = context.musicDataStore.edit { it[KEY_RESUME_ON_BT_CONNECT] = enabled }

    suspend fun setLyricsPriority(priority: String) = context.musicDataStore.edit { it[KEY_LYRICS_PRIORITY] = priority }
    suspend fun setLyricsFontSize(size: Int) = context.musicDataStore.edit { it[KEY_LYRICS_FONT_SIZE] = size }
    suspend fun setLyricsFontWeight(weight: String) = context.musicDataStore.edit { it[KEY_LYRICS_FONT_WEIGHT] = weight }
    suspend fun setLyricsCentered(centered: Boolean) = context.musicDataStore.edit { it[KEY_LYRICS_CENTERED] = centered }
    suspend fun setLyricsKaraoke(enabled: Boolean) = context.musicDataStore.edit { it[KEY_LYRICS_KARAOKE] = enabled }

    suspend fun setExplicitFilter(filter: Boolean) = context.musicDataStore.edit { it[KEY_EXPLICIT_FILTER] = filter }
    suspend fun setPreferredLanguage(lang: String) = context.musicDataStore.edit { it[KEY_PREFERRED_LANGUAGE] = lang }
    suspend fun setKeepScreenOn(keep: Boolean) = context.musicDataStore.edit { it[KEY_KEEP_SCREEN_ON] = keep }
    suspend fun setSeekIntervalSec(sec: Int) = context.musicDataStore.edit { it[KEY_SEEK_INTERVAL_SEC] = sec }
    suspend fun setShakeToSkip(shake: Boolean) = context.musicDataStore.edit { it[KEY_SHAKE_TO_SKIP] = shake }

    suspend fun setAiRecommendations(enabled: Boolean) = context.musicDataStore.edit { it[KEY_AI_RECOMMENDATIONS] = enabled }
    suspend fun setAiTranslateLyrics(enabled: Boolean) = context.musicDataStore.edit { it[KEY_AI_TRANSLATE_LYRICS] = enabled }
    suspend fun setDataSaver(enabled: Boolean) = context.musicDataStore.edit { it[KEY_DATA_SAVER] = enabled }
    suspend fun setOfflineMode(offline: Boolean) = context.musicDataStore.edit { it[KEY_OFFLINE_MODE] = offline }
    suspend fun setLibrarySort(sort: String) = context.musicDataStore.edit { it[KEY_LIBRARY_SORT] = sort }
}
