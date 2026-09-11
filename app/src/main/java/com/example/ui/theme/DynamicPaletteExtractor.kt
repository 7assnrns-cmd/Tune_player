package com.example.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.example.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Immutable
data class ExtractedPalette(
    val dominant: Color = Color(0xFF1E1B4B),
    val vibrant: Color = Color(0xFF38BDF8),
    val darkVibrant: Color = Color(0xFF0F172A),
    val lightVibrant: Color = Color(0xFF93C5FD),
    val accent: Color = Color(0xFF67E8F9),
    val backgroundTop: Color = Color(0xFF131127),
    val backgroundBottom: Color = Color(0xFF07090E)
)

object DynamicPaletteManager {

    private val cache = mutableMapOf<String, ExtractedPalette>()

    suspend fun extractFromSong(context: Context, song: Song?): ExtractedPalette = withContext(Dispatchers.IO) {
        if (song == null) return@withContext ExtractedPalette()

        cache[song.id]?.let { return@withContext it }

        var bitmap: Bitmap? = null

        // 1. Try artworkUri
        if (!song.artworkUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(song.artworkUri)
                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                        decoder.setTargetSize(128, 128)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                // Ignore and try next source
            }
        }

        // 2. Try mediaUri album art if local
        if (bitmap == null && !song.mediaUri.startsWith("http")) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.parse(song.mediaUri))
                val artBytes = retriever.embeddedPicture
                retriever.release()
                if (artBytes != null) {
                    val rawBitmap = android.graphics.BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                    if (rawBitmap != null) {
                        bitmap = Bitmap.createScaledBitmap(rawBitmap, 128, 128, false)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        val result = if (bitmap != null) {
            try {
                val palette = Palette.from(bitmap)
                    .maximumColorCount(16)
                    .generate()

                val dominantInt = palette.getDominantColor(0xFF1E1B4B.toInt())
                val vibrantInt = palette.getVibrantColor(0xFF38BDF8.toInt())
                val darkVibrantInt = palette.getDarkVibrantColor(0xFF0F172A.toInt())
                val lightVibrantInt = palette.getLightVibrantColor(0xFF93C5FD.toInt())
                val mutedInt = palette.getMutedColor(0xFF334155.toInt())

                val dominantColor = Color(dominantInt)
                val vibrantColor = Color(vibrantInt)
                val darkVibrantColor = Color(darkVibrantInt)
                val lightVibrantColor = Color(lightVibrantInt)

                // Produce dynamic harmonious background gradient colors
                val topColor = darkVibrantColor.copy(alpha = 0.9f)
                val bottomColor = Color(0xFF06080F)

                ExtractedPalette(
                    dominant = dominantColor,
                    vibrant = vibrantColor,
                    darkVibrant = darkVibrantColor,
                    lightVibrant = lightVibrantColor,
                    accent = vibrantColor,
                    backgroundTop = topColor,
                    backgroundBottom = bottomColor
                )
            } catch (e: Exception) {
                fallbackFromSong(song)
            }
        } else {
            fallbackFromSong(song)
        }

        cache[song.id] = result
        result
    }

    private fun fallbackFromSong(song: Song): ExtractedPalette {
        // Deterministic lush palette based on song title hash
        val hash = (song.title + song.artist).hashCode()
        val hue = (hash.ushr(1) % 360).toFloat()
        
        val color1 = Color.hsl(hue, 0.65f, 0.22f)
        val color2 = Color.hsl((hue + 40f) % 360f, 0.75f, 0.60f)
        val top = Color.hsl(hue, 0.50f, 0.12f)
        val bottom = Color(0xFF07090E)

        return ExtractedPalette(
            dominant = color1,
            vibrant = color2,
            darkVibrant = top,
            lightVibrant = color2,
            accent = color2,
            backgroundTop = top,
            backgroundBottom = bottom
        )
    }
}

/**
 * Composable helper that dynamically extracts and animates colors smoothly (500ms - 800ms)
 * when the current playing song changes.
 */
@Composable
fun rememberAnimatedDynamicPalette(currentSong: Song?): AnimatedDynamicPalette {
    val context = LocalContext.current
    var currentPalette by remember { mutableStateOf(ExtractedPalette()) }

    LaunchedEffect(currentSong?.id) {
        currentPalette = DynamicPaletteManager.extractFromSong(context, currentSong)
    }

    val animatedDominant by animateColorAsState(
        targetValue = currentPalette.dominant,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "anim_dominant"
    )

    val animatedVibrant by animateColorAsState(
        targetValue = currentPalette.vibrant,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "anim_vibrant"
    )

    val animatedDarkVibrant by animateColorAsState(
        targetValue = currentPalette.darkVibrant,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "anim_dark_vibrant"
    )

    val animatedAccent by animateColorAsState(
        targetValue = currentPalette.accent,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "anim_accent"
    )

    val animatedBgTop by animateColorAsState(
        targetValue = currentPalette.backgroundTop,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "anim_bg_top"
    )

    val animatedBgBottom by animateColorAsState(
        targetValue = currentPalette.backgroundBottom,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "anim_bg_bottom"
    )

    val dynamicBackgroundBrush = remember(animatedBgTop, animatedBgBottom, animatedDominant) {
        Brush.verticalGradient(
            colors = listOf(
                animatedBgTop,
                animatedDominant.copy(alpha = 0.25f),
                animatedBgBottom
            )
        )
    }

    return remember(
        animatedDominant,
        animatedVibrant,
        animatedDarkVibrant,
        animatedAccent,
        animatedBgTop,
        animatedBgBottom,
        dynamicBackgroundBrush
    ) {
        AnimatedDynamicPalette(
            dominant = animatedDominant,
            vibrant = animatedVibrant,
            darkVibrant = animatedDarkVibrant,
            accent = animatedAccent,
            backgroundTop = animatedBgTop,
            backgroundBottom = animatedBgBottom,
            backgroundBrush = dynamicBackgroundBrush
        )
    }
}

@Immutable
data class AnimatedDynamicPalette(
    val dominant: Color,
    val vibrant: Color,
    val darkVibrant: Color,
    val accent: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val backgroundBrush: Brush
)
