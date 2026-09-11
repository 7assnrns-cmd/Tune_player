package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

// ==========================================
// GLASSMORPHISM DESIGN SYSTEM CONSTANTS
// ==========================================

object GlassTheme {
    val BlurRadius: Dp = 25.dp
    
    // Background translucent fills
    val GlassBgLight = Color.White.copy(alpha = 0.10f)
    val GlassBgDark = Color.Black.copy(alpha = 0.30f)
    val GlassBgCard = Color.White.copy(alpha = 0.08f)
    val GlassBgDock = Color.Black.copy(alpha = 0.35f)
    val GlassBgElevated = Color.White.copy(alpha = 0.12f)

    // Border highlights (1.dp translucent white)
    val BorderGlass = Color.White.copy(alpha = 0.18f)
    val BorderGlassHighlight = Color.White.copy(alpha = 0.32f)
    val BorderGlassSubtle = Color.White.copy(alpha = 0.08f)

    // Border stroke
    val DefaultBorderStroke = BorderStroke(1.dp, BorderGlass)
    val HighlightBorderStroke = BorderStroke(1.dp, BorderGlassHighlight)

    // Corners
    val CornerRadiusCard = 24.dp
    val CornerRadiusLarge = 28.dp
    val CornerRadiusDock = 50 // Percent (Pill)
    val CardShape = RoundedCornerShape(CornerRadiusCard)
    val LargeCardShape = RoundedCornerShape(CornerRadiusLarge)
    val DockShape = RoundedCornerShape(CornerRadiusDock)

    // Colors
    val ActiveDockIcon = Color(0xFF38BDF8) // Light Blue Glow
    val InactiveDockIcon = Color(0xFFF1F5F9) // White/Light
    val GlassGlow = Color(0xFF38BDF8).copy(alpha = 0.25f)
}

/**
 * Modifier extension to apply frosted glass effect with Haze and subtle specular reflection.
 */
fun Modifier.glassEffect(
    hazeState: HazeState? = null,
    shape: Shape = GlassTheme.CardShape,
    backgroundColor: Color = GlassTheme.GlassBgCard,
    borderColor: Color = GlassTheme.BorderGlass,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.25f),
        spotColor = Color.Black.copy(alpha = 0.35f)
    )
    .then(
        if (hazeState != null) {
            Modifier.hazeChild(
                state = hazeState,
                shape = shape,
                style = HazeStyle(
                    backgroundColor = backgroundColor,
                    tints = emptyList(),
                    blurRadius = GlassTheme.BlurRadius
                )
            )
        } else {
            Modifier.background(backgroundColor, shape)
        }
    )
    .clip(shape)
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor.copy(alpha = 0.35f),
                borderColor.copy(alpha = 0.12f),
                borderColor.copy(alpha = 0.04f)
            ),
            start = Offset(0f, 0f),
            end = Offset(200f, 400f)
        ),
        shape = shape
    )

/**
 * Standard Reusable Frosted Glass Card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    shape: Shape = GlassTheme.CardShape,
    backgroundColor: Color = GlassTheme.GlassBgCard,
    borderColor: Color = GlassTheme.BorderGlass,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "glass_card_press_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .glassEffect(
                hazeState = hazeState,
                shape = shape,
                backgroundColor = backgroundColor,
                borderColor = borderColor,
                borderWidth = borderWidth,
                elevation = elevation
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

/**
 * Floating Pill-shaped Glass Dock for bottom navigation
 * Active Icon: Light Blue Glow (Color(0xFF38BDF8))
 * Inactive: Crisp White (Color(0xFFF1F5F9))
 * Elevated 16.dp from bottom edge
 */
@Composable
fun GlassDock(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    isSettingsOpen: Boolean = false,
    isSearchOpen: Boolean = false
) {
    val dockItems = listOf(
        DockItemData(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        DockItemData(Screen.Library, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
        DockItemData(Screen.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search)
    )

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = GlassTheme.DockShape,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = GlassTheme.ActiveDockIcon.copy(alpha = 0.25f)
                )
                .then(
                    if (hazeState != null) {
                        Modifier.hazeChild(
                            state = hazeState,
                            shape = GlassTheme.DockShape,
                            style = HazeStyle(
                                backgroundColor = GlassTheme.GlassBgDock,
                                tints = emptyList(),
                                blurRadius = GlassTheme.BlurRadius
                            )
                        )
                    } else Modifier
                )
                .clip(GlassTheme.DockShape)
                .background(GlassTheme.GlassBgDock)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassTheme.BorderGlassHighlight,
                            GlassTheme.BorderGlass,
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = GlassTheme.DockShape
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("floating_glass_dock")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                dockItems.forEach { item ->
                    val isSelected = when (item.screen) {
                        Screen.Search -> isSearchOpen || (currentScreen == Screen.Search && !isSettingsOpen)
                        else -> currentScreen == item.screen && !isSettingsOpen && !isSearchOpen
                    }
                    DockIconButton(
                        isSelected = isSelected,
                        label = item.title,
                        selectedIcon = item.selectedIcon,
                        unselectedIcon = item.unselectedIcon,
                        onClick = {
                            if (item.screen == Screen.Search && onOpenSearch != null) {
                                onOpenSearch()
                            } else {
                                onSelectScreen(item.screen)
                            }
                        },
                        testTag = "dock_item_${item.screen.route}"
                    )
                }

                // Settings dock trigger
                DockIconButton(
                    isSelected = isSettingsOpen,
                    label = "Settings",
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    onClick = onOpenSettings,
                    testTag = "dock_item_settings"
                )
            }
        }
    }
}

private data class DockItemData(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
private fun DockIconButton(
    isSelected: Boolean,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val tintColor by animateColorAsState(
        targetValue = if (isSelected) GlassTheme.ActiveDockIcon else GlassTheme.InactiveDockIcon,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "dock_icon_tint"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else if (isSelected) 1.08f else 1.0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "dock_icon_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .drawBehind {
                if (isSelected) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GlassTheme.ActiveDockIcon.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            radius = size.maxDimension * 0.7f
                        )
                    )
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * Reusable Glass Button
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassTheme.GlassBgCard,
    borderColor: Color = GlassTheme.BorderGlass,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(120),
        label = "glass_btn_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(4.dp, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 1.4f).coerceAtMost(0.9f)),
                        backgroundColor
                    )
                )
            )
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
