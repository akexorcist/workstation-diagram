package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.graphics.Path

data class Connection(
    val path: Path,
    val connector: DeviceCoordinate.Connector,
)
