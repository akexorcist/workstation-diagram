package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Config(
    val minimumHorizontalDistanceToDevice: Dp,
    val minimumVerticalDistanceToDevice: Dp,
    val minimumDistanceBetweenLine: Dp,
    val minimumStartLineDistance: Dp,
    val isAnimationOn: Boolean,
)

val DefaultConfig = Config(
    minimumHorizontalDistanceToDevice = 140.dp,
    minimumVerticalDistanceToDevice = 30.dp,
    minimumDistanceBetweenLine = 30.dp,
    minimumStartLineDistance = 140.dp,
    isAnimationOn = true,
)

data class DebugConfig(
    val visible: Boolean,
    val lineIndex: Int,
    val showWorkspaceArea: Boolean,
    val showDeviceArea: Boolean,
    val showOverlapBoundArea: Boolean,
    val showConnectorArea: Boolean,
    val showAllConnectionLines: Boolean,
    val showLineConnectionPoint: Boolean,
    val disableLineOptimization: Boolean,
)

val DefaultDebugConfig = DebugConfig(
    visible = false,
    lineIndex = 0,
    showWorkspaceArea = false,
    showDeviceArea = false,
    showOverlapBoundArea = false,
    showConnectorArea = false,
    showAllConnectionLines = true,
    showLineConnectionPoint = false,
    disableLineOptimization = false,
)