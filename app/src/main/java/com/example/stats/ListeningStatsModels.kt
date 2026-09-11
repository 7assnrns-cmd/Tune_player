package com.example.stats

import com.example.domain.model.Song

enum class StatsTimePeriod(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

data class DailyListeningBucket(
    val dayLabel: String,
    val minutesListened: Int,
    val songsCount: Int
)

data class ListeningOverview(
    val totalTimeMinutes: Long = 485L,
    val totalSongsPlayed: Int = 112,
    val totalSkips: Int = 8,
    val currentStreakDays: Int = 5,
    val dailyBuckets: List<DailyListeningBucket> = emptyList(),
    val topSongs: List<Pair<Song, Int>> = emptyList(),
    val topArtists: List<Pair<String, Int>> = emptyList(),
    val topGenres: List<Pair<String, Int>> = emptyList()
)
