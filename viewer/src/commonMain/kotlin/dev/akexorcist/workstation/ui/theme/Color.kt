package dev.akexorcist.workstation.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ThemeColor {
    // Base colors
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray850 = Color(0xFF333333)
    val Gray900 = Color(0xFF212121)
    val Gray1000 = Color(0xFF111111)
    
    // Purple colors
    val Purple40 = Color(0xFF6650a4)
    val Purple50 = Color(0xFFEDE7F6)
    val Purple80 = Color(0xFFD0BCFF)
    val Purple100 = Color(0xFFD1C4E9)
    val Purple200 = Color(0xFFB39DDB)
    val Purple500 = Color(0xFF673AB7)
    val Purple700 = Color(0xFF512DA8)
    
    // Blue colors
    val Blue50 = Color(0xFFE3F2FD)
    val Blue100 = Color(0xFFBBDEFB)
    val DimBlue500 = Color(0xFF30419A)
    
    // Teal colors
    val Teal50 = Color(0xFFE0F2F1)
    val Teal100 = Color(0xFFB2DFDB)
    val Teal500 = Color(0xFF009688)
    val DimTeal500 = Color(0xFF377474)
    
    // Pink colors
    val Pink50 = Color(0xFFFCE4EC)
    val Pink100 = Color(0xFFF8BBD0)
    val Pink500 = Color(0xFFE91E63)
    val DimPink500 = Color(0xFFA3538D)
    val DimPink600 = Color(0xFF502946)
    
    // Indigo colors
    val Indigo50 = Color(0xFFE8EAF6)
    val Indigo100 = Color(0xFFC5CAE9)
    val DimIndigo500 = Color(0xFF594A8E)
    val DimIndigo600 = Color(0xFF352C55)
    
    // Amber colors
    val Amber100 = Color(0xFFFFECB3)
    val DimAmber500 = Color(0xFFFAAA20)
    
    // Basic colors
    val White = Color.White
    val Black = Color.Black
    val Transparent = Color(0x00FFFFFF)
}

data class ConnectionLineComponent(
    val inputActiveColor: Color,
    val inputInactiveColor: Color,
    val outputActiveColor: Color,
    val outputInactiveColor: Color,
    val inputBackgroundActiveColor: Color,
    val inputBackgroundInactiveColor: Color,
    val outputBackgroundActiveColor: Color,
    val outputBackgroundInactiveColor: Color,
    val spacingColor: Color,
)

data class ContentColor(
    val text: Color,
    val icon: Color,
    val background: Color,
    val outerBackground: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val onSurfaceSecondary: Color,
    val primary: Color,
    val onPrimary: Color,
    val shadow: Color,
    val border: Color,
    val outlinedButtonColors: @Composable () -> ButtonColors,
    val iconButtonColors: @Composable () -> IconButtonColors,
    val hub: Color,
    val peripheral: Color,
    val centralDevice: Color,
    val connection: ConnectionLineComponent,
)

val lightContentColor = ContentColor(
    text = ThemeColor.Gray900,
    icon = ThemeColor.Gray800,
    background = ThemeColor.Gray50,
    outerBackground = ThemeColor.Gray50,
    surface = ThemeColor.White,
    surfaceVariant = ThemeColor.Gray100,
    onSurface = ThemeColor.Gray900,
    onSurfaceVariant = ThemeColor.Gray800,
    onSurfaceSecondary = ThemeColor.Gray600,
    primary = ThemeColor.Purple40,
    onPrimary = ThemeColor.White,
    shadow = ThemeColor.Black.copy(alpha = 0.1f),
    border = ThemeColor.Gray900,
    outlinedButtonColors = {
        ButtonDefaults.outlinedButtonColors(
            containerColor = ThemeColor.Transparent,
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Transparent,
            disabledContentColor = ThemeColor.Gray500,
        )
    },
    iconButtonColors = {
        IconButtonDefaults.iconButtonColors(
            containerColor = ThemeColor.Purple50,
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Gray200,
            disabledContentColor = ThemeColor.Gray500
        )
    },
    hub = ThemeColor.Teal100,
    peripheral = ThemeColor.Amber100,
    centralDevice = ThemeColor.Blue100,
    connection = ConnectionLineComponent(
        inputActiveColor = ThemeColor.Indigo100,
        inputInactiveColor = ThemeColor.Indigo100.copy(alpha = 0.25f),
        outputActiveColor = ThemeColor.Pink100,
        outputInactiveColor = ThemeColor.Pink100.copy(alpha = 0.25f),
        inputBackgroundActiveColor = ThemeColor.Indigo50.copy(alpha = 0.5f),
        inputBackgroundInactiveColor = ThemeColor.Indigo50.copy(alpha = 0.2f),
        outputBackgroundActiveColor = ThemeColor.Pink50.copy(alpha = 0.5f),
        outputBackgroundInactiveColor = ThemeColor.Pink50.copy(alpha = 0.2f),
        spacingColor = ThemeColor.White
    )
)

val darkContentColor = ContentColor(
    text = ThemeColor.Gray200,
    icon = ThemeColor.Gray300,
    background = ThemeColor.Gray1000,
    outerBackground = ThemeColor.Gray900,
    surface = ThemeColor.Gray850,
    surfaceVariant = ThemeColor.Gray800,
    onSurface = ThemeColor.Gray200,
    onSurfaceVariant = ThemeColor.Gray300,
    onSurfaceSecondary = ThemeColor.Gray500,
    primary = ThemeColor.Purple80,
    onPrimary = ThemeColor.Black,
    shadow = ThemeColor.Black.copy(alpha = 0.2f),
    border = ThemeColor.Gray100,
    outlinedButtonColors = {
        ButtonDefaults.outlinedButtonColors(
            containerColor = ThemeColor.Transparent,
            contentColor = ThemeColor.Gray200,
            disabledContainerColor = ThemeColor.Transparent,
            disabledContentColor = ThemeColor.Gray600
        )
    },
    iconButtonColors = {
        IconButtonDefaults.iconButtonColors(
            containerColor = ThemeColor.Purple200,
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Gray700,
            disabledContentColor = ThemeColor.Gray600
        )
    },
    hub = ThemeColor.DimTeal500,
    peripheral = ThemeColor.DimAmber500,
    centralDevice = ThemeColor.DimBlue500,
    connection = ConnectionLineComponent(
        inputActiveColor = ThemeColor.DimIndigo500,
        inputInactiveColor = ThemeColor.DimIndigo500.copy(alpha = 0.25f),
        outputActiveColor = ThemeColor.DimPink500,
        outputInactiveColor = ThemeColor.DimPink500.copy(alpha = 0.25f),
        inputBackgroundActiveColor = ThemeColor.DimIndigo600.copy(alpha = 0.5f),
        inputBackgroundInactiveColor = ThemeColor.DimIndigo600.copy(alpha = 0.2f),
        outputBackgroundActiveColor = ThemeColor.DimPink600.copy(alpha = 0.5f),
        outputBackgroundInactiveColor = ThemeColor.DimPink600.copy(alpha = 0.2f),
        spacingColor = ThemeColor.Gray900
    )
)
