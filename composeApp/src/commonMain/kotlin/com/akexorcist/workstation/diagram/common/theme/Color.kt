package com.akexorcist.workstation.diagram.common.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    val Teal100 = Color(0xFFB2DFDB)
    val Teal200 = Color(0xFF80CBC4)
    val Teal300 = Color(0xFF4DB6AC)
    val Amber100 = Color(0xFFFFECB3)
    val Amber200 = Color(0xFFFFE082)
    val Amber300 = Color(0xFFFFD54F)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray900 = Color(0xFF212121)
    val Pink50 = Color(0xFFFCE4EC)
    val Purple50 = Color(0xFFEDE7F6)
    val White = Color.White
    val Black = Color.Black
    val Transparent = Color.Transparent
}

data class DeviceComponent(
    val shape: Shape,
    val buttonColors: @Composable () -> ButtonColors,
) {
}

object DeviceComponentTheme {
    val Computer = DeviceComponent(
        shape = RoundedCornerShape(8.dp),
        buttonColors = { DeviceColor.computer() }
    )
    val Hub = DeviceComponent(
        shape = RoundedCornerShape(8.dp),
        buttonColors = { DeviceColor.hub() }
    )
    val End = DeviceComponent(
        shape = RoundedCornerShape(8.dp),
        buttonColors = { DeviceColor.end() }
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
