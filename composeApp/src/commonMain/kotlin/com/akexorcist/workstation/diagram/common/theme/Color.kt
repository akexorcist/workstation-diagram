package com.akexorcist.workstation.diagram.common.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object ThemeColor {
    val Red100 = Color(0xFFFFCDD2)
    val Red200 = Color(0xFFEF9A9A)
    val Red300 = Color(0xFFE57373)
    val Teal50 = Color(0xFFE0F2F1)
    val Teal100 = Color(0xFFB2DFDB)
    val Teal200 = Color(0xFF80CBC4)
    val Teal300 = Color(0xFF4DB6AC)
    val Teal500 = Color(0xFF009688)
    val Green50 = Color(0xFFE8F5E9)
    val Green100 = Color(0xFFC8E6C9)
    val Green300 = Color(0xFF81C784)
    val Amber100 = Color(0xFFFFECB3)
    val Amber200 = Color(0xFFFFE082)
    val Amber300 = Color(0xFFFFD54F)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray900 = Color(0xFF212121)
    val Pink50 = Color(0xFFFCE4EC)
    val Pink100 = Color(0xFFF8BBD0)
    val Pink200 = Color(0xFFF48FB1)
    val Purple50 = Color(0xFFEDE7F6)
    val Purple100 = Color(0xFFD1C4E9)
    val Purple200 = Color(0xFFB39DDB)
    val Indigo50 = Color(0xFFE8EAF6)
    val Indigo100 = Color(0xFFC5CAE9)
    val Indigo200 = Color(0xFF9FA8DA)
    val White = Color.White
    val Black = Color.Black
    val Transparent = Color.Transparent
}

data class DeviceComponent(
    val cornerRadius: Dp,
    val buttonColors: @Composable () -> ButtonColors,
)

object DeviceComponentTheme {
    val Computer = DeviceComponent(
        cornerRadius = 8.dp,
        buttonColors = { DeviceColor.computer() },
    )
    val Hub = DeviceComponent(
        cornerRadius = 8.dp,
        buttonColors = { DeviceColor.hub() },
    )
    val End = DeviceComponent(
        cornerRadius = 8.dp,
        buttonColors = { DeviceColor.end() },
    )
}

data class ConnectorComponent(
    val cornerRadius: Dp,
    val color: Color,
)

object ConnectorComponentTheme {
    val Input = ConnectorComponent(
        cornerRadius = 8.dp,
        color = ThemeColor.Indigo50,
    )
    val Output = ConnectorComponent(
        cornerRadius = 8.dp,
        color = ThemeColor.Pink50,
    )
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

object ConnectionLineComponentTheme {
    val default = ConnectionLineComponent(
        inputActiveColor = ThemeColor.Indigo100,
        inputInactiveColor = ThemeColor.Indigo100.copy(alpha = 0.25f),
        outputActiveColor = ThemeColor.Pink100,
        outputInactiveColor = ThemeColor.Pink100.copy(alpha = 0.25f),
        inputBackgroundActiveColor = ThemeColor.Indigo50.copy(alpha = 0.5f),
        inputBackgroundInactiveColor = ThemeColor.Indigo50.copy(alpha = 0.2f),
        outputBackgroundActiveColor = ThemeColor.Pink50.copy(alpha = 0.5f),
        outputBackgroundInactiveColor = ThemeColor.Pink50.copy(alpha = 0.2f),
        spacingColor = ThemeColor.White,
    )
}

object DeviceColor {
    @Composable
    fun computer() = ButtonDefaults.buttonColors(
        containerColor = ThemeColor.Red100,
        contentColor = ThemeColor.Gray900,
        disabledContainerColor = ThemeColor.Gray200,
        disabledContentColor = ThemeColor.Gray500,
    )

    @Composable
    fun hub() = ButtonDefaults.buttonColors(
        containerColor = ThemeColor.Teal100,
        contentColor = ThemeColor.Gray900,
        disabledContainerColor = ThemeColor.Gray200,
        disabledContentColor = ThemeColor.Gray500,
    )

    @Composable
    fun end() = ButtonDefaults.buttonColors(
        containerColor = ThemeColor.Amber100,
        contentColor = ThemeColor.Gray900,
        disabledContainerColor = ThemeColor.Gray200,
        disabledContentColor = ThemeColor.Gray500,
    )
}
