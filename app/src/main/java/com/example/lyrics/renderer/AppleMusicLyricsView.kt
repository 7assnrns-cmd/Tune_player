package com.example.lyrics.renderer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lyrics.model.LyricDocument
import com.example.lyrics.model.LyricLine
import com.example.lyrics.model.LyricRole
import com.example.lyrics.model.LyricWord
import kotlinx.coroutines.delay

@Composable
fun AppleMusicLyricsView(
    document: LyricDocument?,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    fontSizeSp: Int = 24,
    fontWeight: FontWeight = FontWeight.Bold,
    isCentered: Boolean = false,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    dimColor: Color = Color.White.copy(alpha = 0.40f)
) {
    if (document == null || document.isEmpty) {
        EmptyLyricsPlaceholder(modifier = modifier)
        return
    }

    val activeLineIndex by remember(document, currentPositionMs) {
        derivedStateOf { document.findActiveLineIndex(currentPositionMs) }
    }

    val listState = rememberLazyListState()
    var userScrolledManually by remember { mutableStateOf(false) }

    // When the user drags or scrolls, note manual scroll
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            userScrolledManually = true
        }
    }

    // Auto scroll when active line changes if user has not taken over manual scroll
    LaunchedEffect(activeLineIndex, userScrolledManually) {
        if (!userScrolledManually && activeLineIndex in document.lines.indices) {
            // Scroll so the active line sits comfortably in the upper-middle viewing area
            val targetIndex = (activeLineIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex, scrollOffset = -80)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = 80.dp, bottom = 160.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.fillMaxSize().testTag("lyrics_list")
        ) {
            itemsIndexed(document.lines, key = { index, line -> "${line.startTimeMs}_$index" }) { index, line ->
                val isActive = index == activeLineIndex
                val isPast = line.isPast(currentPositionMs)

                LyricLineRow(
                    line = line,
                    isActive = isActive,
                    isPast = isPast,
                    currentPositionMs = currentPositionMs,
                    fontSizeSp = fontSizeSp,
                    fontWeight = fontWeight,
                    isCentered = isCentered,
                    highlightColor = highlightColor,
                    dimColor = dimColor,
                    onClick = {
                        userScrolledManually = false
                        onSeekTo(line.startTimeMs)
                    }
                )
            }
        }

        // Floating "Sync to current lyric" re-sync button when user has scrolled away
        AnimatedVisibility(
            visible = userScrolledManually && activeLineIndex >= 0,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                onClick = {
                    userScrolledManually = false
                },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                shadowElevation = 8.dp,
                modifier = Modifier.testTag("sync_lyrics_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync Lyrics",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sync to lyric",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricLineRow(
    line: LyricLine,
    isActive: Boolean,
    isPast: Boolean,
    currentPositionMs: Long,
    fontSizeSp: Int,
    fontWeight: FontWeight,
    isCentered: Boolean,
    highlightColor: Color,
    dimColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.03f else 1.0f,
        animationSpec = tween(300),
        label = "lineScale"
    )

    val targetAlpha = when {
        isActive -> 1.0f
        isPast -> 0.35f
        else -> 0.65f
    }
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(250),
        label = "lineAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = alphaAnim
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isCentered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        if (line.hasWordTiming && isActive) {
            // High fidelity Apple Music word-by-word animation
            SynchronizedWordsFlow(
                words = line.words,
                currentPositionMs = currentPositionMs,
                fontSizeSp = fontSizeSp,
                fontWeight = fontWeight,
                isCentered = isCentered,
                highlightColor = highlightColor,
                dimColor = dimColor
            )
        } else {
            // Line-level highlighting
            Text(
                text = line.text,
                fontSize = if (line.isBackgroundVocal) (fontSizeSp - 4).sp else fontSizeSp.sp,
                fontWeight = if (line.isBackgroundVocal) FontWeight.Normal else fontWeight,
                fontStyle = if (line.isBackgroundVocal) FontStyle.Italic else FontStyle.Normal,
                color = when {
                    isActive -> Color.White
                    isPast -> dimColor.copy(alpha = 0.30f)
                    else -> dimColor
                },
                textAlign = if (isCentered) TextAlign.Center else TextAlign.Start,
                lineHeight = (fontSizeSp + 8).sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SynchronizedWordsFlow(
    words: List<LyricWord>,
    currentPositionMs: Long,
    fontSizeSp: Int,
    fontWeight: FontWeight,
    isCentered: Boolean,
    highlightColor: Color,
    dimColor: Color
) {
    FlowRow(
        horizontalArrangement = if (isCentered) Arrangement.Center else Arrangement.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        words.forEach { word ->
            ProgressiveKaraokeWord(
                word = word,
                currentPositionMs = currentPositionMs,
                fontSizeSp = fontSizeSp,
                fontWeight = fontWeight,
                highlightColor = highlightColor,
                dimColor = dimColor
            )
        }
    }
}

@Composable
private fun ProgressiveKaraokeWord(
    word: LyricWord,
    currentPositionMs: Long,
    fontSizeSp: Int,
    fontWeight: FontWeight,
    highlightColor: Color,
    dimColor: Color
) {
    val progress = word.progressAt(currentPositionMs)
    val isSung = currentPositionMs >= word.endTimeMs
    val isCurrentlySinging = currentPositionMs in word.startTimeMs..word.endTimeMs

    // Use progressive gradient clipping across the word text to create the iconic Apple Music sweep fill
    Box(modifier = Modifier.padding(end = 4.dp)) {
        // Base subdued word text
        Text(
            text = word.text,
            fontSize = if (word.isBackgroundVocal) (fontSizeSp - 4).sp else fontSizeSp.sp,
            fontWeight = if (word.isBackgroundVocal) FontWeight.Normal else fontWeight,
            fontStyle = if (word.isBackgroundVocal) FontStyle.Italic else FontStyle.Normal,
            color = if (word.isBackgroundVocal) dimColor.copy(alpha = 0.5f) else dimColor,
            lineHeight = (fontSizeSp + 8).sp
        )

        // Highlight layer with horizontal fill
        if (progress > 0f) {
            Text(
                text = word.text,
                fontSize = if (word.isBackgroundVocal) (fontSizeSp - 4).sp else fontSizeSp.sp,
                fontWeight = if (word.isBackgroundVocal) FontWeight.Normal else fontWeight,
                fontStyle = if (word.isBackgroundVocal) FontStyle.Italic else FontStyle.Normal,
                color = Color.White,
                lineHeight = (fontSizeSp + 8).sp,
                modifier = Modifier
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        // Clip text from left to right based on progress
                        val fillWidth = size.width * progress
                        drawRect(
                            color = Color.Black,
                            topLeft = androidx.compose.ui.geometry.Offset(fillWidth, 0f),
                            size = androidx.compose.ui.geometry.Size(size.width - fillWidth, size.height),
                            blendMode = BlendMode.DstOut
                        )
                    }
            )
        }
    }
}

@Composable
private fun EmptyLyricsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Synchronized Lyrics Available",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Load a .ttml or .lrc file from Settings or enjoy the track.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
