package com.akexorcist.workstation.diagram.common.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ThemeColor {
    val Red100 = Color(0xFFFFCDD2)
    val Red200 = Color(0xFFEF9A9A)
    val Red300 = Color(0xFFE57373)
    val Red700 = Color(0xFFD32F2F)
    val Red800 = Color(0xFFC62828)
    val Blue100 = Color(0xFFBBDEFB)
    val DimBlue500 = Color(0xFF3B4FBE)
    val Teal50 = Color(0xFFE0F2F1)
    val Teal100 = Color(0xFFB2DFDB)
    val Teal200 = Color(0xFF80CBC4)
    val Teal300 = Color(0xFF4DB6AC)
    val Teal500 = Color(0xFF009688)
    val Teal600 = Color(0xFF00897B)
    val Teal700 = Color(0xFF00796B)
    val Teal800 = Color(0xFF00695C)
    val DimTeal500 = Color(0xFF408687)
    val Green50 = Color(0xFFE8F5E9)
    val Green100 = Color(0xFFC8E6C9)
    val Green300 = Color(0xFF81C784)
    val Amber100 = Color(0xFFFFECB3)
    val Amber200 = Color(0xFFFFE082)
    val Amber300 = Color(0xFFFFD54F)
    val Amber700 = Color(0xFFFFA000)
    val DimAmber500 = Color(0xFFFBBC4F)
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
    val Pink40 = Color(0xFF7D5260)
    val Pink50 = Color(0xFFFCE4EC)
    val Pink80 = Color(0xFFEFB8C8)
    val Pink100 = Color(0xFFF8BBD0)
    val Pink200 = Color(0xFFF48FB1)
    val Pink700 = Color(0xFFC2185B)
    val Pink800 = Color(0xFFAD1457)
    val DimPink500 = Color(0xFFA3538D)
    val DimPink600 = Color(0xFF502946)
    val Purple40 = Color(0xFF6650a4)
    val Purple50 = Color(0xFFEDE7F6)
    val Purple80 = Color(0xFFD0BCFF)
    val Purple100 = Color(0xFFD1C4E9)
    val Purple200 = Color(0xFFB39DDB)
    val PurpleGrey40 = Color(0xFF625b71)
    val PurpleGrey80 = Color(0xFFCCC2DC)
    val Indigo50 = Color(0xFFE8EAF6)
    val Indigo100 = Color(0xFFC5CAE9)
    val Indigo200 = Color(0xFF9FA8DA)
    val DimIndigo500 = Color(0xFF594A8E)
    val DimIndigo600 = Color(0xFF352C55)

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
    val uiBackground: Color,
    val outerBackground: Color,
    val outerBorder: Color,
    val transparentBackground: Color,
    val selectedBackground: Color,
    val hoveredBackground: Color,
    val outlinedButtonColors: @Composable () -> ButtonColors,
    val computer: Color,
    val computerButton: @Composable () -> ButtonColors,
    val hub: Color,
    val hubButton: @Composable () -> ButtonColors,
    val accessory: Color,
    val accessoryButton: @Composable () -> ButtonColors,
    val input: Color,
    val output: Color,
    val connection: ConnectionLineComponent,
)

val lightContentColor = ContentColor(
    text = ThemeColor.Gray900,
    icon = ThemeColor.Gray900,
    background = ThemeColor.White,
    uiBackground = ThemeColor.White,
    outerBackground = ThemeColor.Gray50,
    outerBorder = ThemeColor.Gray400,
    transparentBackground = ThemeColor.Transparent,
    selectedBackground = ThemeColor.Gray500,
    hoveredBackground = ThemeColor.Gray50,
    outlinedButtonColors = {
        ButtonDefaults.outlinedButtonColors(
            containerColor = ThemeColor.Transparent,
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Transparent,
            disabledContentColor = ThemeColor.Gray500,
        )
    },
    computer = ThemeColor.Blue100,
    computerButton = {
        ButtonDefaults.buttonColors(
            containerColor = ThemeColor.Blue100,
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Gray200,
            disabledContentColor = ThemeColor.Gray500,
        )
    },
    hub = ThemeColor.Teal100,
    hubButton = {
        ButtonDefaults.buttonColors(
            containerColor = ThemeColor.Teal100,
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Gray200,
            disabledContentColor = ThemeColor.Gray500,
        )
    },
    accessory = ThemeColor.Amber100,
    accessoryButton = {
        ButtonDefaults.buttonColors(
            containerColor = ThemeColor.Amber100,
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Gray200,
            disabledContentColor = ThemeColor.Gray500,
        )
    },
    input = ThemeColor.Indigo50,
    output = ThemeColor.Pink50,
    connection = ConnectionLineComponent(
        inputActiveColor = ThemeColor.Indigo100,
        inputInactiveColor = ThemeColor.Indigo100.copy(alpha = 0.25f),
        outputActiveColor = ThemeColor.Pink100,
        outputInactiveColor = ThemeColor.Pink100.copy(alpha = 0.25f),
        inputBackgroundActiveColor = ThemeColor.Indigo50.copy(alpha = 0.5f),
        inputBackgroundInactiveColor = ThemeColor.Indigo50.copy(alpha = 0.2f),
        outputBackgroundActiveColor = ThemeColor.Pink50.copy(alpha = 0.5f),
        outputBackgroundInactiveColor = ThemeColor.Pink50.copy(alpha = 0.2f),
        spacingColor = ThemeColor.White,
    ),
)

val darkContentColor = ContentColor(
    text = ThemeColor.Gray200,
    icon = ThemeColor.Gray200,
    background = ThemeColor.Gray900,
    uiBackground = ThemeColor.Gray850,
    outerBackground = ThemeColor.Gray1000,
    outerBorder = ThemeColor.Gray900,
    transparentBackground = ThemeColor.Transparent,
    selectedBackground = ThemeColor.Gray200,
    hoveredBackground = ThemeColor.Transparent,
    outlinedButtonColors = {
        ButtonDefaults.outlinedButtonColors(
            containerColor = ThemeColor.Transparent,
            contentColor = ThemeColor.Gray200,
            disabledContainerColor = ThemeColor.Transparent,
            disabledContentColor = ThemeColor.Gray600,
        )
    },
    computer = ThemeColor.DimBlue500.copy(alpha = 0.7f),
    computerButton = {
        ButtonDefaults.buttonColors(
            containerColor = ThemeColor.DimBlue500.copy(alpha = 0.7f),
            contentColor = ThemeColor.Gray200,
            disabledContainerColor = ThemeColor.Gray700,
            disabledContentColor = ThemeColor.Gray600,
        )
    },
    hub = ThemeColor.DimTeal500.copy(alpha = 0.7f),
    hubButton = {
        ButtonDefaults.buttonColors(
            containerColor = ThemeColor.DimTeal500.copy(alpha = 0.7f),
            contentColor = ThemeColor.Gray200,
            disabledContainerColor = ThemeColor.Gray700,
            disabledContentColor = ThemeColor.Gray600,
        )
    },
    accessory = ThemeColor.DimAmber500.copy(alpha = 0.7f),
    accessoryButton = {
        ButtonDefaults.buttonColors(
            containerColor = ThemeColor.DimAmber500.copy(alpha = 0.7f),
            contentColor = ThemeColor.Gray900,
            disabledContainerColor = ThemeColor.Gray700,
            disabledContentColor = ThemeColor.Gray600,
        )
    },
    input = ThemeColor.DimIndigo500,
    output = ThemeColor.DimPink500,
    connection = ConnectionLineComponent(
        inputActiveColor = ThemeColor.DimIndigo500,
        inputInactiveColor = ThemeColor.DimIndigo500.copy(alpha = 0.25f),
        outputActiveColor = ThemeColor.DimPink500,
        outputInactiveColor = ThemeColor.DimPink500.copy(alpha = 0.25f),
        inputBackgroundActiveColor = ThemeColor.DimIndigo600.copy(alpha = 0.5f),
        inputBackgroundInactiveColor = ThemeColor.DimIndigo600.copy(alpha = 0.2f),
        outputBackgroundActiveColor = ThemeColor.DimPink600.copy(alpha = 0.5f),
        outputBackgroundInactiveColor = ThemeColor.DimPink600.copy(alpha = 0.2f),
        spacingColor = ThemeColor.Gray900,
    ),
)
