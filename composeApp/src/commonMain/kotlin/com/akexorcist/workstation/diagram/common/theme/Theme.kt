@file:Suppress("FunctionName")

package com.akexorcist.workstation.diagram.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColors(
    primary = ThemeColor.Purple80,
    secondary = ThemeColor.PurpleGrey80,
)

private val LightColorScheme = lightColors(
    primary = ThemeColor.Purple40,
    secondary = ThemeColor.PurpleGrey40,
)

@Composable
fun WorkstationDiagramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val themeColor = when {
        darkTheme -> darkContentColor
        else -> lightContentColor
    }

    CompositionLocalProvider(
        LocalThemeColor provides themeColor,
        LocalComponentSpec provides defaultComponentSpec,
    ) {
        MaterialTheme(
            colors = colorScheme,
            content = content
        )
    }
}

object WorkstationDiagramTheme {
    val themeColor: ContentColor
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeColor.current

    val componentSpec: ComponentSpec
        @Composable
        @ReadOnlyComposable
        get() = LocalComponentSpec.current
}

internal val LocalThemeColor = staticCompositionLocalOf { lightContentColor }
internal val LocalComponentSpec = staticCompositionLocalOf { defaultComponentSpec }