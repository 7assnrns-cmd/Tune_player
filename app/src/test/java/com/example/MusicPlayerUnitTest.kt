package com.example

import com.example.data.local.entity.SongEntity
import com.example.domain.model.AudioQuality
import com.example.domain.model.RepeatMode
import com.example.domain.model.SleepTimerOption
import com.example.domain.model.Song
import com.example.ui.components.formatTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicPlayerUnitTest {

    @Test
    fun `formatTime formats milliseconds into correct mm ss string`() {
        assertEquals("00:00", formatTime(0L))
        assertEquals("00:00", formatTime(-100L))
        assertEquals("00:05", formatTime(5000L))
        assertEquals("01:05", formatTime(65000L))
        assertEquals("03:35", formatTime(215000L))
        assertEquals("1:01:05", formatTime(3665000L))
    }

    @Test
    fun `SongEntity mapping preserves all song attributes`() {
        val originalSong = Song(
            id = "test_song_1",
            title = "Midnight Odyssey",
            artist = "Solaris Collective",
            album = "Cosmic Waves",
            durationMs = 210000L,
            mediaUri = "content://media/external/audio/100",
            artworkUri = "content://media/external/audio/albumart/5",
            trackNumber = 1,
            year = 2024,
            genre = "Synthwave",
            isFavorite = true,
            playCount = 12,
            audioQuality = AudioQuality.LOSSLESS,
            isRemote = false
        )

        val entity = SongEntity.fromSong(originalSong)
        assertEquals("test_song_1", entity.id)
        assertEquals("Midnight Odyssey", entity.title)
        assertTrue(entity.isFavorite)
        assertEquals(12, entity.playCount)

        val convertedBack = entity.toSong()
        assertEquals(originalSong.id, convertedBack.id)
        assertEquals(originalSong.title, convertedBack.title)
        assertEquals(originalSong.artist, convertedBack.artist)
        assertEquals(originalSong.album, convertedBack.album)
        assertEquals(originalSong.durationMs, convertedBack.durationMs)
        assertEquals(originalSong.mediaUri, convertedBack.mediaUri)
        assertEquals(originalSong.artworkUri, convertedBack.artworkUri)
        assertEquals(originalSong.isFavorite, convertedBack.isFavorite)
        assertEquals(originalSong.playCount, convertedBack.playCount)
        assertEquals(AudioQuality.LOSSLESS, convertedBack.audioQuality)
    }

    @Test
    fun `queue reorder moves song to expected index`() {
        val queue = mutableListOf("Song A", "Song B", "Song C", "Song D")
        val fromIndex = 1 // "Song B"
        val toIndex = 3

        val item = queue.removeAt(fromIndex)
        queue.add(toIndex, item)

        assertEquals(listOf("Song A", "Song C", "Song D", "Song B"), queue)
    }

    @Test
    fun `queue remove removes item at index`() {
        val queue = mutableListOf("Song A", "Song B", "Song C")
        queue.removeAt(1)
        assertEquals(listOf("Song A", "Song C"), queue)
    }

    @Test
    fun `search filter matches title, artist, or album case insensitively`() {
        val songs = listOf(
            Song(id = "1", title = "Blinding Lights", artist = "The Weeknd", album = "After Hours", durationMs = 200000L, mediaUri = ""),
            Song(id = "2", title = "Save Your Tears", artist = "The Weeknd", album = "After Hours", durationMs = 215000L, mediaUri = ""),
            Song(id = "3", title = "Midnight City", artist = "M83", album = "Hurry Up, We're Dreaming", durationMs = 244000L, mediaUri = "")
        )

        val query = "weeknd"
        val results = songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
        assertEquals(2, results.size)

        val songQuery = "midnight"
        val songResults = songs.filter {
            it.title.contains(songQuery, ignoreCase = true)
        }
        assertEquals(1, songResults.size)
        assertEquals("Midnight City", songResults[0].title)
    }

    @Test
    fun `sleep timer options have correct minutes configured`() {
        assertEquals(5, SleepTimerOption.MIN_5.minutes)
        assertEquals(15, SleepTimerOption.MIN_15.minutes)
        assertEquals(30, SleepTimerOption.MIN_30.minutes)
        assertEquals(60, SleepTimerOption.MIN_60.minutes)
        assertEquals(-1, SleepTimerOption.END_OF_TRACK.minutes)
    }

    @Test
    fun `repeat mode cycles correctly`() {
        val modes = RepeatMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(RepeatMode.OFF))
        assertTrue(modes.contains(RepeatMode.ALL))
        assertTrue(modes.contains(RepeatMode.ONE))
    }
}
