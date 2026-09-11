package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.glassEffect
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * State holder for interruptible, 100% reversible drag transitions.
 */
class DraggableOverlayState(
    val animatableProgress: Animatable<Float, AnimationVector1D> = Animatable(0f)
) {
    val progress: Float
        get() = animatableProgress.value

    val isVisible: Boolean
        get() = progress > 0.001f

    val isOpen: Boolean
        get() = progress >= 0.999f

    suspend fun open() {
        animatableProgress.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    suspend fun close() {
        animatableProgress.animateTo(
            targetValue = 0.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    suspend fun snapProgress(target: Float) {
        animatableProgress.snapTo(target.coerceIn(0f, 1f))
    }

    suspend fun onDragEnd(velocity: Float) {
        // High downward velocity triggers dismissal regardless of current progress
        val target = when {
            velocity > 1200f -> 0.0f
            velocity < -1200f -> 1.0f
            progress > 0.5f -> 1.0f
            else -> 0.0f
        }
        
        animatableProgress.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = if (target == 1f) Spring.DampingRatioLowBouncy else Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialVelocity = velocity / 1000f
        )
    }
}

@Composable
fun rememberDraggableOverlayState(initiallyOpen: Boolean = false): DraggableOverlayState {
    val animatable = remember { Animatable(if (initiallyOpen) 1f else 0f) }
    return remember(animatable) { DraggableOverlayState(animatable) }
}

/**
 * Full Draggable Frosted Glass Overlay (e.g. for Settings or Queue)
 * 
 * Interruptibility Guarantee:
 * - Dragging updates progress proportionally.
 * - Releasing triggers spring animation to 0 or 1 from the EXACT current progress without jumping.
 * - Back button or backdrop tap reverses animation directly towards 0.0.
 */
@Composable
fun DraggableGlassOverlay(
    state: DraggableOverlayState,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Handle system back gesture
    BackHandler(enabled = state.isVisible) {
        coroutineScope.launch {
            state.close()
            onDismissRequest()
        }
    }

    if (!state.isVisible) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("draggable_glass_overlay_container")
    ) {
        val totalHeightPx = constraints.maxHeight.toFloat()
        val progress = state.progress

        // Subtle Glass Ambient Scrim (keeps home screen 100% visible behind blur)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = (progress * 0.20f).coerceIn(0f, 0.20f)
                }
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    coroutineScope.launch {
                        state.close()
                        onDismissRequest()
                    }
                }
                .testTag("overlay_scrim")
        )

        // Overlay Sheet Content (Offset from bottom)
        val offsetY = ((1.0f - progress) * totalHeightPx).roundToInt()

        val draggableState = rememberDraggableState { delta ->
            coroutineScope.launch {
                val currentProgress = state.progress
                val deltaProgress = -delta / totalHeightPx
                state.snapProgress(currentProgress + deltaProgress)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, offsetY) }
                .graphicsLayer {
                    // Subtle scale for depth effect
                    scaleX = 0.96f + (0.04f * progress)
                    scaleY = 0.96f + (0.04f * progress)
                    alpha = (progress * 1.5f).coerceIn(0f, 1f)
                }
                .glassEffect(
                    hazeState = hazeState,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    backgroundColor = Color.Black.copy(alpha = 0.30f),
                    borderColor = GlassTheme.BorderGlassHighlight,
                    borderWidth = 1.dp,
                    elevation = 20.dp
                )
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        state.onDragEnd(-velocity)
                        if (state.progress < 0.5f) {
                            onDismissRequest()
                        }
                    }
                )
                .testTag("draggable_glass_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                // Drag Handle Bar Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.40f))
                    )
                }

                // Overlay Title Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                state.close()
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                            .testTag("overlay_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Child scrollable content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    content()
                }
            }
        }
    }
}
