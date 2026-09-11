package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletPrimaryContainer,
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E3E4B),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = RoseFavorite,
    onTertiary = Color.White,
    background = NocturneBackground,
    onBackground = TextPrimaryDark,
    surface = NocturneSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NocturneSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = NocturneBorder,
    outlineVariant = NocturneSurfaceElevated
)

private val LightColorScheme = lightColorScheme(
    primary = VioletLightPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletLightPrimaryContainer,
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF0F766E),
    tertiary = RoseFavorite,
    onTertiary = Color.White,
    background = PearlBackground,
    onBackground = TextPrimaryLight,
    surface = PearlSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = PearlSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = PearlBorder,
    outlineVariant = PearlSurfaceElevated
)

@Composable
fun MusicPlayerTheme(
    themeMode: String = "DARK", // "SYSTEM", "DARK", "LIGHT"
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
