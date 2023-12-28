package com.akexorcist.workstation.diagram.common.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ComponentSpec(
    val computer: Device,
    val hub: Device,
    val accessory: Device,
    val input: Connector,
    val output: Connector,
) {
    data class Device(
        val cornerRadius: Dp,
    )
    data class Connector(
        val cornerRadius: Dp,
    )
}

val defaultComponentSpec = ComponentSpec(
    computer = ComponentSpec.Device(
        cornerRadius = 8.dp,
    ),
    hub = ComponentSpec.Device(
        cornerRadius = 8.dp,
    ),
    accessory = ComponentSpec.Device(
        cornerRadius = 8.dp,
    ),
    input = ComponentSpec.Connector(
        cornerRadius = 8.dp,
    ),
    output = ComponentSpec.Connector(
        cornerRadius = 8.dp,
    ),
)