package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Port
import dev.akexorcist.workstation.data.model.PortType
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

/**
 * A declarative Compose component that renders a single port indicator.
 */
@Composable
fun PortNode(
    port: Port,
    zoom: Float,
    modifier: Modifier = Modifier
) {
    val portColor = getPortColor(port.type)
    val portSize = 8f * zoom

    Box(modifier = modifier) {
        // Port background
        Box(
            modifier = Modifier
                .size((portSize + 4).dp)
                .background(
                    color = portColor.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )

        // Port center
        Box(
            modifier = Modifier
                .size(portSize.dp)
                .background(portColor, CircleShape)
        )
    }
}

@Composable
private fun getPortColor(type: PortType): Color {
    return when (type) {
        PortType.USB_C -> ThemeColor.DimBlue500
        PortType.USB_A_2_0,
        PortType.USB_A_3_0,
        PortType.USB_A_3_1,
        PortType.USB_A_3_2 -> WorkstationTheme.themeColor.hub
        PortType.HDMI,
        PortType.HDMI_2_1,
        PortType.DISPLAY_PORT,
        PortType.MINI_HDMI,
        PortType.MICRO_HDMI -> WorkstationTheme.themeColor.peripheral
        PortType.ETHERNET -> ThemeColor.Purple500
        PortType.AUX -> ThemeColor.Pink500
        PortType.POWER -> ThemeColor.DimAmber500
    }
}