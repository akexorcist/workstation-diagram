@file:Suppress("FunctionName")

package dev.akexorcist.workstation.editor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColorScheme(
    primary = darkContentColor.primary,
    onPrimary = darkContentColor.onPrimary,
    surface = darkContentColor.surface,
    onSurface = darkContentColor.onSurface,
    background = darkContentColor.background
)

private val LightColorScheme = lightColorScheme(
    primary = lightContentColor.primary,
    onPrimary = lightContentColor.onPrimary,
    surface = lightContentColor.surface,
    onSurface = lightContentColor.onSurface,
    background = lightContentColor.background
)

@Composable
fun WorkstationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // These will be recomputed when darkTheme changes, triggering recomposition
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val themeColor = if (darkTheme) darkContentColor else lightContentColor
    
    // Provide the theme color through composition local
    CompositionLocalProvider(
        LocalThemeColor provides themeColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

/**
 * Provides access to theme colors throughout the application.
 * Any component using this will automatically recompose when the theme changes.
 */
object WorkstationTheme {
    /**
     * Current theme colors. Will cause recomposition when theme changes.
     */
    val themeColor: ContentColor
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeColor.current
}

/**
 * The composition local used to provide theme colors to the composition tree.
 * Default to light theme, but will be provided with the appropriate theme in WorkstationTheme.
 */
internal val LocalThemeColor = staticCompositionLocalOf { lightContentColor }