package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.MusicPlayerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleSong = Song(
      id = "test_1",
      title = "Midnight Horizon",
      artist = "Aura Soundscapes",
      album = "Neon Reflections",
      durationMs = 215000L,
      mediaUri = ""
    )

    composeTestRule.setContent {
      MusicPlayerTheme {
        SongListItem(
          song = sampleSong,
          isCurrentSong = false,
          isPlaying = false,
          onClick = {},
          onToggleFavorite = {},
          onAddToPlaylist = {},
          onAddToQueue = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
