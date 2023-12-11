package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Config(
    val minimumHorizontalDistanceToDevice: Dp,
    val minimumVerticalDistanceToDevice: Dp,
    val minimumDistanceBetweenLine: Dp,
    val minimumStartLineDistance: Dp,
)

val DefaultConfig = Config(
    minimumHorizontalDistanceToDevice = 140.dp,
    minimumVerticalDistanceToDevice = 30.dp,
    minimumDistanceBetweenLine = 60.dp,
    minimumStartLineDistance = 140.dp,
)

data class DebugConfig(
    val lineIndex: Int,
    val zoomScale: Float,
    val showWorkspaceArea: Boolean,
    val showDeviceArea: Boolean,
    val showOverlapBoundArea: Boolean,
    val showConnectorArea: Boolean,
    val showAllConnectionLines: Boolean,
    val showLineConnectionPoint: Boolean,
)

val DefaultDebugConfig = DebugConfig(
    lineIndex = 17,
    zoomScale = 1f,
    showWorkspaceArea = false,
    showDeviceArea = false,
    showOverlapBoundArea = false,
    showConnectorArea = false,
    showAllConnectionLines = true,
    showLineConnectionPoint = false,
)